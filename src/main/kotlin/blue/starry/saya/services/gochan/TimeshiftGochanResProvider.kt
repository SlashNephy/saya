package blue.starry.saya.services.gochan

import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlin.time.hours

class TimeshiftGochanResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val client: GochanClient,
    private val boards: List<Definitions.Board>
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    private val allowedRange = 3.hours

    override suspend fun fetch(): Unit = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        boards.asFlow().flatMapConcat { board ->
            AutoPastGochanThreadSelector.enumerate(client, board, startAt, endAt, allowedRange)
        }.mapNotNull { thread ->
            val dat = try {
                client.get2chScDat(thread.list.server, thread.list.board, thread.id)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                return@mapNotNull null
            }

            GochanDatParser.parse(dat).filter { res ->
                res.time.toEpochSecond() in startAt until endAt
            }.map { res ->
                res.toSayaComment(
                    source = "5ch 過去ログ [${thread.title}]",
                    sourceUrl = thread.url
                )
            }
        }.toList().flatten().sortedBy {
            it.time
        }.also {
            comments.withLock { list ->
                list.addAll(it)
            }
        }
    }
}
