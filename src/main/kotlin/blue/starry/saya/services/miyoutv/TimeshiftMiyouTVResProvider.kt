package blue.starry.saya.services.miyoutv

import blue.starry.saya.models.Definitions
import blue.starry.saya.services.TimeshiftCommentProviderImpl
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class TimeshiftMiyouTVResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val api: MiyouTVApi,
    private val id: String
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    override suspend fun fetch() {
        comments.withLock { list ->
            list.clear()
        }

        // unit 秒ずつ分割して取得
        val unit = 600
        val duration = (endAt - startAt).toInt()

        (0 until duration / unit).asFlow().map { i ->
            api.getComments(
                id,
                (startAt + unit * i) * 1000,
                minOf(startAt + unit * (i + 1), endAt) * 1000
            ).data.comments.map {
                it.toSayaComment()
            }
        }.collect {
            comments.withLock { list ->
                list.addAll(it)
            }
        }
    }
}
