package blue.starry.saya.services.gochan

import blue.starry.saya.common.DefaultDictionary
import blue.starry.saya.common.asThreadSafe
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.LiveCommentProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import mu.KotlinLogging
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration

class LiveGochanResProvider(
    override val channel: Definitions.Channel,
    private val client: GochanClient,
    private val boards: List<Definitions.Board>
) : LiveCommentProvider {
    override val queue = MutableSharedFlow<Comment>()
    override val subscription = LiveCommentProvider.Subscription()

    private val logger = KotlinLogging.createSayaLogger("saya.services.5ch[${channel.name}]")
    private val threadSearchInterval = Duration.seconds(10)
    private val resCollectInterval = Duration.seconds(5)
    private val threadLimit = 5

    override suspend fun start() = coroutineScope {
        joinAll(
            launch {
                while (isActive) {
                    try {
                        doSearchThreadsLoop()
                    } catch (e: CancellationException) {
                        break
                    } catch (t: Throwable) {
                        logger.error(t) { "error in doSearchThreadsLoop" }
                    }

                    delay(threadSearchInterval)
                }
            },
            launch {
                while (isActive) {
                    try {
                        doCollectResLoop()
                    } catch (e: CancellationException) {
                        break
                    } catch (t: Throwable) {
                        logger.error(t) { "error in doCollectResLoop" }
                    }

                    delay(resCollectInterval)
                }
            }
        )
    }

    private val threadLoaders = mutableMapOf<GochanThreadAddress, GochanDatThreadLoader>().asThreadSafe()
    private val subjects = DefaultDictionary<Definitions.Board, MutableMap<GochanThreadAddress, GochanSubjectItem>> {
        mutableMapOf()
    }.asThreadSafe()

    private suspend fun doSearchThreadsLoop() {
        boards.forEach { board ->
            val items = AutoGochanThreadSelector.enumerate(client, board, limit = threadLimit)

            subjects.withLock { subjects ->
                subjects[board].clear()

                for (item in items) {
                    val address = GochanThreadAddress(board.server, board.board, item.threadId)
                    subjects[board][address] = item

                    logger.trace { item }
                }
            }
        }
    }

    private suspend fun doCollectResLoop() {
        subjects.withLock { subjects ->
            for ((board, map) in subjects) {
                for ((address, item) in map) {
                    val loader = threadLoaders.withLock { safeThreadLoaders ->
                        safeThreadLoaders.getOrPut(address) {
                            GochanDatThreadLoader(address)
                        }
                    }

                    val now = ZonedDateTime.now()
                    loader.fetch(client).filter {
                        it.time.plusSeconds(15).isAfter(now)
                    }.forEach {
                        queue.emit(
                            it.toSayaComment(
                                source = "5ch [${item.title}]",
                                sourceUrl = "https://${board.server}.5ch.net/test/read.cgi/${board.board}/${item.threadId}"
                            )
                        )

                        logger.trace { it }
                        delay(Random.nextLong(0..300L))
                    }
                }
            }
        }
    }

    override fun close() {
        client.close()
    }
}
