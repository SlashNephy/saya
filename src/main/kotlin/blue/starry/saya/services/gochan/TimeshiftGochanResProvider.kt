package blue.starry.saya.services.gochan

import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class TimeshiftGochanResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val client: GochanClient,
    private val board: Definitions.Board
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    override suspend fun fetch() = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        AutoPastGochanThreadSelector.enumerate(client, channel, board, startAt, endAt).map {
            val dat = client.get2chScDat(it.list.server, it.list.board, it.id)

            GochanDatParser.parse(dat).filter { res ->
                res.time.toEpochSecond() in startAt until endAt
            }.map { res ->
                res.toSayaComment(
                    source = "5ch 過去ログ [${it.title}]",
                    sourceUrl = it.url
                )
            }
        }.collect {
            comments.withLock { list ->
                list.addAll(it)
            }
        }
    }
}
