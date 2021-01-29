package blue.starry.saya.services.comments

import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import blue.starry.saya.services.SayaTwitterClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TwitterHashTagProvider(override val stream: CommentStream, private val tags: Set<String>): CommentProvider {
    private val logger = KotlinLogging.logger("saya.services.twitter#${tags.joinToString(",")}")

    override val subscriptions = AtomicInteger(0)
    override val stats = TwitterHashTagStatisticsProvider(tags.joinToString(",") { "#$it" })
    override val job = GlobalScope.launch {
        SayaTwitterClient.stream.filter(track = tags.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "twitter:connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = createComment(status)
                stream.comments.send(comment)
                stats.add(status)

                logger.trace { status }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "twitter:disconnect" }
            }
        }, false).join()
    }

    private fun createComment(status: Status): Comment {
        return Comment(
            tags.joinToString(",") { "#$it" },
            stats.provide().comments,
            Instant.from(
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(status.createdAtRaw)
            ).epochSecond.toDouble(),
            status.user.name,
            tags.fold(status.text) { r, t -> r.replace("#$t", "") },
            "#ffffff",
            "right",
            "normal",
            emptyList()
        )
    }
}
