package blue.starry.saya.services.gochan

import blue.starry.saya.common.asThreadSafe
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.LiveCommentProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import mu.KotlinLogging
import java.time.ZonedDateTime
import kotlin.time.seconds

class LiveGochanResCommentProvider(
    override val channel: Definitions.Channel,
    private val client: GochanClient,
    private val board: Definitions.Board
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()

    private val logger = KotlinLogging.createSayaLogger("saya.services.5ch[${channel.name}]")
    private val threadSearchInterval = 10.seconds
    private val resCollectInterval = 5.seconds
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
    private val subject = mutableMapOf<GochanThreadAddress, GochanSubjectItem>().asThreadSafe()

    private suspend fun doSearchThreadsLoop() {
        val items = AutoGochanThreadSelector.enumerate(client, channel, board, limit = threadLimit)

        subject.withLock { subject ->
            subject.clear()

            for (item in items) {
                val address = GochanThreadAddress(board.server, board.board, item.threadId)
                subject[address] = item

                logger.trace { item }
            }
        }
    }

    private suspend fun doCollectResLoop() {
        subject.withLock { subject ->
            for ((address, item) in subject) {
                val loader = threadLoaders.withLock { safeThreadLoaders ->
                    safeThreadLoaders.getOrPut(address) {
                        GochanDatThreadLoader(address)
                    }
                }

                val now = ZonedDateTime.now()
                loader.fetch(client).filter {
                    it.time.plusSeconds(15).isAfter(now)
                }.forEach {
                    comments.send(it.toSayaComment("5ch [${item.title}]"))

                    logger.trace { it }
                }
            }
        }
    }
}
