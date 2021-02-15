package blue.starry.saya.services.nicolive

import blue.starry.saya.models.Definitions
import blue.starry.saya.services.TimeshiftCommentProviderImpl
import blue.starry.saya.services.nicojk.NicoJkApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class TimeshiftNicoliveCommentProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    override suspend fun fetch() = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        // unit 秒ずつ分割して取得
        val unit = 600
        val duration = (endAt - startAt).toInt()

        (0 until duration / unit).asFlow().map { i ->
            NicoJkApi.getComments(
                "jk${channel.nicojkId}",
                startAt + unit * i,
                minOf(startAt + unit * (i + 1) - 1, endAt)
            ).packets.map {
                it.chat.toSayaComment(
                    source = "ニコニコ実況過去ログAPI [jk${channel.nicojkId}]",
                    sourceUrl = "https://jikkyo.tsukumijima.net/api/kakolog/jk${channel.nicojkId}?starttime=${it.chat.date}&endtime=${it.chat.date + 1}&format=json"
                )
            }
        }.collect {
            comments.withLock { list ->
                list.addAll(it)
            }
        }
    }
}
