package blue.starry.saya.services.comments.nicolive

import blue.starry.saya.services.comments.Comment
import blue.starry.saya.services.comments.CommentStatisticsProvider
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class NicoLiveStatisticsProvider: CommentStatisticsProvider {
    @Serializable data class Statistics(
        override val comments: Int,
        override val commentsPerMinute: Int,
        val viewers: Int,
        val adPoints: Int,
        val giftPoints: Int
    ): CommentStatisticsProvider.Statistics

    private val comments = AtomicInteger()
    private val commentsTime = AtomicLong()
    private val firstComments = AtomicInteger()
    private val firstCommentsTime = AtomicLong()
    private val commentsPerMinute: Int
        get() {
            val c = comments.get()
            val fc = firstComments.get()
            val ct = commentsTime.get()
            val fct = firstCommentsTime.get()
            if (ct == fct) {
                return 0
            }

            return 60 * (c - fc) / (ct - fct).toInt()
        }
    private val viewers = AtomicInteger()
    private val adPoints = AtomicInteger()
    private val giftPoints = AtomicInteger()

    override fun provide(): Statistics {
        return Statistics(
            comments = comments.get(),
            commentsPerMinute = commentsPerMinute,
            viewers = viewers.get(),
            adPoints = adPoints.get(),
            giftPoints = giftPoints.get()
        )
    }

    fun update(data: NicoLiveWebSocketSystemJson.Data) {
        viewers.set(data.viewers)
        adPoints.set(data.adPoints)
        giftPoints.set(data.giftPoints)
    }

    // stats の精度がよくないのでコメントの no から計算
    fun update(comment: Comment) {
        if (firstComments.get() == 0) {
            firstComments.set(comment.no)
        }
        if (firstCommentsTime.get() == 0L) {
            firstCommentsTime.set(comment.time)
        }

        comments.set(comment.no)
        commentsTime.set(comment.time)
    }
}
