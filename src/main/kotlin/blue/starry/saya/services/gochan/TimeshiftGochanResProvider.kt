package blue.starry.saya.services.gochan

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import kotlin.time.Duration

class TimeshiftGochanResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val client: GochanClient,
    private val boards: List<Definitions.Board>
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    override suspend fun fetch(): Unit = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        val logger = KotlinLogging.createSayaLogger("saya.TimeshiftGochanResProvider")

        boards.asFlow()
            .flatMapConcat { board ->
                AutoPastGochanThreadSelector.enumerate(client, board, startAt, endAt, Duration.hours(3))
            }
            .mapNotNull { thread ->
                val dat = try {
                    client.get2chScDat(thread.list.server, thread.list.board, thread.id)
                } catch (e: CancellationException) {
                    logger.debug { "cancel in fetch" }
                    throw e
                } catch (t: Throwable) {
                    logger.error(t) { "error in fetch" }
                    return@mapNotNull null
                }

                GochanDatParser.parse(dat)
                    .filter { res ->
                        res.time.toEpochSecond() in startAt until endAt
                    }
                    .map { res ->
                        res.toSayaComment(
                            source = "5ch 過去ログ [${thread.title}]",
                            sourceUrl = thread.url
                        )
                    }.toList()
            }.toList().flatten().sortedBy {
                it.time
            }.also {
                comments.withLock { list ->
                    list.addAll(it)
                }
            }
    }

    override fun close() {
        super.close()
        client.close()
    }
}
