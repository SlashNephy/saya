package blue.starry.saya.services.gochan

import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.time.hours

class TimeshiftGochanResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val client: GochanClient,
    private val boards: List<Definitions.Board>
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    private val allowedRange = 3.hours

    override suspend fun fetch() = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        boards.asFlow().flatMapConcat { board ->
            AutoPastGochanThreadSelector.enumerate(client, board, startAt, endAt, allowedRange)
        }.map {
            val dat = client.get2chScDat(it.list.server, it.list.board, it.id)

            GochanDatParser.parse(dat).filter { res ->
                res.time.toEpochSecond() in startAt until endAt
            }.map { res ->
                res.toSayaComment(
                    source = "5ch 過去ログ [${it.title}]",
                    sourceUrl = it.url
                )
            }
        }.toList().flatten().sortedBy {
            it.time
        }.also {
            comments.withLock { list ->
                list.addAll(it)
            }
        }

        Unit
    }
}
