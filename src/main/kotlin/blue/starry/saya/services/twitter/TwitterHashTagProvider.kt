package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.CommentProvider
import kotlinx.coroutines.channels.BroadcastChannel
import mu.KotlinLogging
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class TwitterHashTagProvider(
    override val channel: JikkyoChannel,
    override val comments: BroadcastChannel<Comment>,
    private val tags: Set<String>,
    private val client: ApiClient
): CommentProvider {
    private val logger = KotlinLogging.logger("saya.services.twitter#${tags.joinToString(",")}")

    suspend fun start() {
        try {
            connect()
        } catch (t: Throwable) {
            logger.trace { "cancel: TwitterHashTagProvider: ${t.stackTraceToString()}" }
        }
    }

    private suspend fun connect() {
        client.stream.filter(track = tags.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "twitter:connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = createComment(status)
                comments.send(comment)

                logger.trace { status }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "twitter:disconnect" }
            }
        }, true).join()
    }

    private fun createComment(status: Status): Comment {
        return Comment(
            tags.joinToString(",") { "#$it" },
            Instant.from(
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(status.createdAtRaw)
            ).epochSecond,
            0,
            status.user.name,
            tags.fold(status.text) { r, t -> r.replace("#$t", "") },
            "#ffffff",
            Comment.Position.right,
            Comment.Size.normal
        )
    }
}
