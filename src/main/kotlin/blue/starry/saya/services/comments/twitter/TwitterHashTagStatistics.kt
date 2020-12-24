package blue.starry.saya.services.comments.twitter

import blue.starry.penicillin.extensions.idObj
import blue.starry.penicillin.models.Status
import blue.starry.saya.services.comments.CommentStatisticsProvider
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TwitterHashTagStatistics(private val source: String): CommentStatisticsProvider {
    @Serializable data class Statistics(
        override val source: String,
        override val comments: Int,
        override val commentsPerMinute: Int
    ): CommentStatisticsProvider.Statistics

    private val comments = AtomicInteger()
    private val commentsTime = AtomicLong()
    private val firstCommentsTime = AtomicLong()
    private val commentsPerMinute: Int
        get() {
            val c = comments.get()
            val ct = commentsTime.get()
            val fct = firstCommentsTime.get()
            if (ct == fct) {
                return 0
            }

            return 60 * c / (ct - fct).toInt()
        }

    override fun provide(): Statistics {
        return Statistics(
            source = source,
            comments = comments.get(),
            commentsPerMinute = commentsPerMinute
        )
    }

    fun add(status: Status) {
        val time = status.idObj.epochTimeMillis / 1000
        if (firstCommentsTime.get() == 0L) {
            firstCommentsTime.set(time)
        }

        comments.getAndIncrement()
        commentsTime.set(time)
    }
}
