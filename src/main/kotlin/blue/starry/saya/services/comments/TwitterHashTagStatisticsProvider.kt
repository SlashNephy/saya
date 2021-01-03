package blue.starry.saya.services.comments

import blue.starry.penicillin.extensions.createdAt
import blue.starry.penicillin.extensions.instant
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.TwitterHashTagStatistics
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TwitterHashTagStatisticsProvider(private val source: String): CommentStatisticsProvider {
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

    override fun provide(): TwitterHashTagStatistics {
        return TwitterHashTagStatistics(
            source = source,
            comments = comments.get(),
            commentsPerMinute = commentsPerMinute
        )
    }

    fun add(status: Status) {
        val time = status.createdAt.instant.epochSecond
        if (firstCommentsTime.get() == 0L) {
            firstCommentsTime.set(time)
        }

        comments.getAndIncrement()
        commentsTime.set(time)
    }
}
