package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.exceptions.PenicillinException
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.search
import blue.starry.penicillin.endpoints.search.search
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.execute
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.extensions.rateLimit
import blue.starry.penicillin.models.Status
import blue.starry.saya.common.Env
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.LiveCommentProvider
import io.ktor.util.date.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import kotlin.time.seconds
import kotlin.time.toKotlinDuration

class LiveTweetProvider(
    override val channel: Definitions.Channel,
    private val client: ApiClient,
    private val keywords: Set<String>
): LiveCommentProvider {
    override val queue = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()
    private val logger = KotlinLogging.createSayaLogger("saya.services.twitter[${channel.name}]")

    override suspend fun start() = coroutineScope {
        if (Env.TWITTER_PREFER_STREAMING_API) {
            while (isActive) {
                try {
                    doStreamLoop(client, keywords)
                } catch (e: CancellationException) {
                    return@coroutineScope
                // レートリミット
                } catch (e: PenicillinException) {
                    break
                } catch (t: Throwable) {
                    logger.error(t) { "error in doStreamLoop" }
                }

                delay(5.seconds)
            }
        }

        while (isActive) {
            try {
                doSearchLoop(client, keywords)
            } catch (e: CancellationException) {
                break
            } catch (t: Throwable) {
                logger.trace(t) { "error in doSearchLoop" }
            }

            delay(5.seconds)
        }
    }

    private suspend fun doStreamLoop(client: ApiClient, keywords: Set<String>) {
        client.stream.filter(track = keywords.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = status.toSayaComment("Twitter Filter", keywords) ?: return
                queue.send(comment)

                logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "disconnect" }

                if (cause != null) {
                    throw cause
                }
            }
        })
    }

    private var lastId: Long? = null
    private var lastIdLock = Mutex()

    private suspend fun doSearchLoop(client: ApiClient, keywords: Set<String>) {
        lastIdLock.withLock {
            val response = client.search.search(
                query = keywords.joinToString(" OR "),
                sinceId = lastId
            ).execute()

            if (lastId != null) {
                for (status in response.result.statuses) {
                    val comment = status.toSayaComment("Twitter 検索", keywords) ?: continue
                    queue.send(comment)

                    logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
                }
            }

            lastId = response.result.statuses.firstOrNull()?.id?.plus(1) ?: lastId
            val limit = response.rateLimit

            if (limit == null || limit.remaining == 0) {
                delay(15.seconds)
            } else {
                val duration = Duration.between(Instant.now(), limit.resetAt.toJvmDate().toInstant()).toKotlinDuration()
                val safeRate = duration / limit.remaining
                logger.trace { "Ratelimit ${limit.remaining}/${limit.limit}: Sleep $safeRate ($keywords)" }

                delay(safeRate)
            }
        }
    }
}
