package blue.starry.saya.services.miyoutv

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.repeatMap
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.TimeshiftCommentProviderImpl
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration

class TimeshiftMiyouTVResProvider(
    channel: Definitions.Channel,
    startAt: Long,
    endAt: Long,
    private val api: MiyouTVApi,
    private val id: String
): TimeshiftCommentProviderImpl(channel, startAt, endAt) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.miyoutv[${channel.name}]")

    override suspend fun fetch(): Unit = coroutineScope {
        comments.withLock { list ->
            list.clear()
        }

        // 10分ずつ分割して取得
        val unit = 600
        val duration = (endAt - startAt).toInt()

        val newComments = repeatMap(duration / unit) { index ->
            async {
                repeat(5) {
                    while (isActive) {
                        try {
                            return@async fetchCommentsInRangeOf(
                                startAt = (startAt + unit * index) * 1000,
                                endAt = minOf(startAt + unit * (index + 1), endAt) * 1000,
                                offset = unit * index * 1000
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
        return api.getComments(id, startAt, endAt).data.comments.map {
            it.toSayaComment(
                seconds = it.time / 1000.0 - startAt + offset
            )
        }
    }
}
