package blue.starry.saya.services.comments.twitter

import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.core.streaming.listener.SampleStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.endpoints.stream.sample
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import blue.starry.saya.services.comments.CommentProvider
import blue.starry.saya.services.comments.CommentStream
import blue.starry.saya.services.SayaTwitterClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TwitterHashTagProvider(override val stream: CommentStream, private val tag: String): CommentProvider {
    companion object {
        const val SampleStreamTag = "SAMPLE"
    }

    private val logger = KotlinLogging.logger("saya.services.twitter#${tag}")

    override val comments = BroadcastChannel<Comment>(1)
    override val subscriptions = AtomicInteger(0)
    override val stats = TwitterHashTagStatisticsProvider("#${tag}")
    override val job = GlobalScope.launch {
        if (tag != SampleStreamTag) {
            SayaTwitterClient.stream.filter(track = listOf(tag)).listen(object: FilterStreamListener {
                override suspend fun onConnect() {
                    logger.debug { "twitter:connect" }
                }

                override suspend fun onStatus(status: Status) {
                    val comment = createComment(status)
                    comments.send(comment)
                    stats.add(status)

                    logger.trace { status }
                }

                override suspend fun onDisconnect(cause: Throwable?) {
                    logger.debug(cause) { "twitter:disconnect" }
                }
            }, false)
        } else {
            SayaTwitterClient.stream.sample.listen(object: SampleStreamListener {
                override suspend fun onConnect() {
                    logger.debug { "twitter:connect" }
                }

                override suspend fun onStatus(status: Status) {
                    val comment = createComment(status)
                    comments.send(comment)
                    stats.add(status)

                    logger.trace { status }
                }

                override suspend fun onDisconnect(cause: Throwable?) {
                    logger.debug(cause) { "twitter:disconnect" }
                }
            }, false)
        }.join()
    }

    private fun createComment(status: Status): Comment {
        return Comment(
            "#$tag",
            stats.provide().comments,
            Instant.from(
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(status.createdAtRaw)
            ).epochSecond,
            status.user.name,
            status.text.replace("#$tag", ""),
            "#ffffff",
            "right",
            "normal",
            emptyList()
        )
    }
}
