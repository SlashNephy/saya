package blue.starry.saya.services.comments

import blue.starry.saya.models.Comment
import blue.starry.saya.models.NicoCommentStatistics
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import java.util.concurrent.atomic.AtomicInteger

class NicoLiveStatisticsProvider(private val source: String): CommentStatisticsProvider {
    private val comments = AtomicInteger()
    @Volatile private var commentsTime = 0.0
    private val firstComments = AtomicInteger()
    @Volatile private var firstCommentsTime = 0.0
    private val commentsPerMinute: Int
        get() {
            val c = comments.get()
            val fc = firstComments.get()
            val ct = commentsTime
            val fct = firstCommentsTime
            if (ct == fct) {
                return 0
            }

            return 60 * (c - fc) / (ct - fct).toInt()
        }
    private val viewers = AtomicInteger()
    private val adPoints = AtomicInteger()
    private val giftPoints = AtomicInteger()

    override fun provide(): NicoCommentStatistics {
        return NicoCommentStatistics(
            source = source,
            comments = comments.get(),
            commentsPerMinute = commentsPerMinute,
            viewers = viewers.get(),
            adPoints = adPoints.get(),
            giftPoints = giftPoints.get()
        )
    }

    fun update(data: NicoLiveWebSocketSystemJson.Data) {
        viewers.set(data.viewers)
        adPoints.set(data.adPoints ?: 0)
        giftPoints.set(data.giftPoints ?: 0)
    }

    // stats の精度がよくないのでコメントの no から計算
    fun update(comment: Comment) {
        if (firstComments.get() == 0) {
            firstComments.set(comment.no)
        }
        if (firstCommentsTime == 0.0) {
            firstCommentsTime = comment.time
        }

        comments.set(comment.no)
        commentsTime = comment.time
    }
}
