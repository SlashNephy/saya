package blue.starry.saya.services.comments.twitter

import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.createdAt
import blue.starry.penicillin.extensions.instant
import blue.starry.penicillin.extensions.listen
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.services.comments.Comment
import blue.starry.saya.services.comments.CommentProvider
import blue.starry.saya.services.comments.CommentStatisticsProvider
import blue.starry.saya.services.comments.CommentStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging

class TwitterHashTagProvider(override val stream: CommentStream, private val tag: String): CommentProvider {
    private val logger = KotlinLogging.logger("saya.services.twitter.$tag")

    override val comments = BroadcastChannel<Comment>(1)
    override val stats = TwitterHashTagStatistics()
    override val job = GlobalScope.launch {
        twitter.stream.filter(track = listOf("#$tag")).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "twitter:connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = createComment(status)
                comments.send(comment)
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "twitter:disconnect" }
            }
        }).await()
    }

    private fun createComment(status: Status): Comment {
        return Comment("#$tag", stats.comments, status.createdAt.instant.epochSecond, status.user.name, status.text.replace("#$tag", ""), "#ffffff", "right", "normal", emptyList())
    }
}

class TwitterHashTagStatistics: CommentStatisticsProvider {
    override var comments = 0
        private set

    override var commentsPerMinute = 0
        private set
}
