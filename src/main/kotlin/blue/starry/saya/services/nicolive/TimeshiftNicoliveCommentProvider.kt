package blue.starry.saya.services.nicolive

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.repeatMap
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import blue.starry.saya.services.nicojk.NicoJkApi
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.seconds

class TimeshiftNicoliveCommentProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${channel.name}]")

    override suspend fun fetch(): Unit = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        // 30分ずつ分割して取得
        val unit = 1800
        val duration = (endAt - startAt).toInt()

        val newComments = repeatMap(duration / unit) { index ->
            async {
                repeat(5) {
                    while (isActive) {
                        try {
                            return@async fetchCommentsInRangeOf(
                                startAt = startAt + unit * index,
                                endAt = minOf(startAt + unit * (index + 1) - 1, endAt),
                                offset = unit * index
                            )
                        } catch (e: CancellationException) {
                            break
                        } catch (t: Throwable) {
                            logger.error(t) { "Failed to fetch comments. Retry in 3 sec." }
                            delay(Duration.seconds(3))
                        }
                    }
                }

                null
            }
        }.awaitAll().mapNotNull { it }.flatten()

        comments.withLock { comments ->
            comments.addAll(newComments)
        }
    }

    private suspend fun fetchCommentsInRangeOf(startAt: Long, endAt: Long, offset: Int): List<Comment> {
        return NicoJkApi.getComments("jk${channel.nicojkId}", startAt, endAt)
            .packets
            .asSequence()
            .filter { "deleted" !in it.chat.json }
            .map { it.chat }
            .map {
                it.toSayaComment(
                    source = "ニコニコ実況過去ログAPI [jk${channel.nicojkId}]",
                    sourceUrl = "https://jikkyo.tsukumijima.net/api/kakolog/jk${channel.nicojkId}?starttime=${it.date}&endtime=${it.date + 1}&format=json",
                    seconds = ((it.date * 1000 + it.dateUsec / 1000) - startAt * 1000) / 1000.0 + offset
                )
            }
            .toList()
    }
}
