package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.search
import blue.starry.penicillin.endpoints.search.search
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.RateLimit
import blue.starry.penicillin.extensions.execute
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.extensions.rateLimit
import blue.starry.penicillin.models.Status
import blue.starry.saya.common.Env
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

        if (Env.TWITTER_PREFER_STREAMING_API) {
            try {
                doStreamLoop(client, tags)
            } catch (t: Throwable) {
                logger.error(t) { "error in stream" }
            }
        }

        try {
            doSearchLoop(client, tags)
        } catch (t: Throwable) {
            logger.trace(t) { "cancel" }
        }
    }

    private suspend fun doStreamLoop(client: ApiClient, tags: Set<String>) {
        client.stream.filter(track = tags.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = status.toSayaComment("Twitter Filter", tags) ?: return
                comments.send(comment)

                logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "disconnect" }
            }
        })
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

                if (lastId != null) {
                    for (status in response.result.statuses) {
                        val comment = status.toSayaComment("Twitter 検索", tags) ?: continue
                        comments.send(comment)

                        logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
                    }
                }

                lastId = response.result.statuses.firstOrNull()?.id?.plus(1) ?: lastId
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
