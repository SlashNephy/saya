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

    override suspend fun start() = coroutineScope {
        joinAll(
            launch {
                while (isActive) {
                    try {
                        ensureActive()
                        doSearchThreadsLoop()
                    } catch (e: CancellationException) {
                        logger.debug { "cancel: doSearchThreadsLoop" }
                        break
                    } catch (t: Throwable) {
                        logger.error(t) { "error in doSearchThreadsLoop" }
                    }

                    delay(Duration.seconds(5))
                }
            },
            launch {
                while (isActive) {
                    try {
                        ensureActive()
                        doCollectResLoop()
                    } catch (e: CancellationException) {
                        logger.debug { "cancel: doCollectResLoop" }
                        break
                    } catch (t: Throwable) {
                        logger.error(t) { "error in doCollectResLoop" }
                    }

                    delay(Duration.seconds(5))
                }
            }
        )
    }

    private val threadLoaders = mutableMapOf<GochanThreadAddress, GochanDatThreadLoader>().asThreadSafe()
    private val subjects = DefaultDictionary<Definitions.Board, MutableMap<GochanThreadAddress, GochanSubjectItem>> {
        mutableMapOf()
    }.asThreadSafe()

    private suspend fun doSearchThreadsLoop(): Unit = coroutineScope {
        boards.map { board ->
            launch {
                val newItems = AutoGochanThreadSelector.enumerate(client, board)
                    .map { item ->
                        val address = GochanThreadAddress(board.server, board.board, item.threadId)
                        address to item
                    }.toMap()

                subjects.withLock { subjects ->
                    subjects[board].clear()
                    subjects[board].putAll(newItems)
                }

                logger.trace { newItems }
            }
        }.joinAll()
    }

    private suspend fun doCollectResLoop(): Unit = coroutineScope {
        subjects.withLock { subjects ->
            subjects.flatMap { (board, items) ->
                items.map { (address, item) ->
                    val loader = threadLoaders.withLock { safeThreadLoaders ->
                        safeThreadLoaders.getOrPut(address) {
                            GochanDatThreadLoader(address)
                        }
                    }

                    launch {
                        val now = ZonedDateTime.now()
                        loader.fetch(client)
                            .filter {
                                it.time.plusSeconds(15).isAfter(now)
                            }
                            .forEach {
                                queue.emit(
                                    it.toSayaComment(
                                        source = "5ch [${item.title}]",
                                        sourceUrl = "https://${board.server}.5ch.net/test/read.cgi/${board.board}/${item.threadId}"
                                    )
                                )

                                logger.trace { it }
                                delay(Random.nextLong(0..500L))
                            }
                    }
                }
            }.joinAll()
        }
    }

    override fun close() {
        client.close()
    }
}
