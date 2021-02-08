package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.search
import blue.starry.penicillin.endpoints.search.search
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.RateLimit
import blue.starry.penicillin.extensions.execute
import blue.starry.penicillin.extensions.rateLimit
import blue.starry.penicillin.models.Status
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.LiveCommentProvider
import blue.starry.saya.services.SayaTwitterClient
import io.ktor.util.date.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import kotlin.time.seconds
import kotlin.time.toKotlinDuration

class TwitterHashTagProvider(
    override val channel: JikkyoChannel
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()
    private val logger = KotlinLogging.createSayaLogger("saya.services.twitter.${channel.name}]")

    override suspend fun start() {
        val client = SayaTwitterClient ?: return
        val tags = channel.hashtags
        if (tags.isEmpty()) {
            return
        }

        try {
            doStreamLoop(client, tags)
            doSearchLoop(client, tags)
        } catch (t: Throwable) {
            logger.trace(t) { "cancel" }
        }
    }

    private suspend fun doStreamLoop(client: ApiClient, tags: Set<String>) {
        // TODO: Penicillin 側の API をあとでなおす, coroutineContext を渡す仕様に変更
        client.stream.filter(track = tags.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = status.toSayaComment(tags)
                comments.send(comment)

                logger.trace { status }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "disconnect" }
            }
        }, false).cancel()
    }

    private suspend fun doSearchLoop(client: ApiClient, tags: Set<String>) {
        var lastId: Long? = null

        while (true) {
            var limit: RateLimit? = null

            try {
                val response = client.search.search(
                    query = tags.joinToString(" OR ") { "#$it" },
                    sinceId = lastId
                ).execute()

                response.result.statuses.forEach { status ->
                    val comment = status.toSayaComment(tags)
                    comments.send(comment)

                    logger.trace { status }
                }

                lastId = response.result.statuses.lastOrNull()?.id
                limit = response.rateLimit
            } catch (t: Throwable) {
                logger.error(t) { "error in doSearchLoop" }
            } finally {
                if (limit == null || limit.remaining == 0) {
                    delay(15.seconds)
                } else {
                    val duration = Duration.between(Instant.now(), limit.resetAt.toJvmDate().toInstant()).toKotlinDuration()
                    val safeRate = duration / limit.remaining
                    logger.trace { "Ratelimit ${limit.remaining}/${limit.limit}: Sleep $safeRate ($tags)" }

                    delay(safeRate)
                }
            }
        }
    }
}
