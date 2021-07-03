package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.exceptions.PenicillinException
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.search
import blue.starry.penicillin.endpoints.search.search
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.extensions.lang
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.extensions.rateLimit
import blue.starry.penicillin.extensions.use
import blue.starry.penicillin.models.Status
import blue.starry.saya.common.Env
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.comments.LiveCommentProvider
import blue.starry.saya.services.createSayaTwitterClient
import io.ktor.util.date.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.apache.http.ConnectionClosedException
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

class LiveTweetProvider(
    override val channel: Definitions.Channel,
    private val keywords: Set<String>
): LiveCommentProvider {
    override val queue = MutableSharedFlow<Comment>()
    override val subscription = LiveCommentProvider.Subscription()
    private val logger = KotlinLogging.createSayaLogger("saya.services.twitter[${channel.name}]")

    override suspend fun start() = coroutineScope {
        if (Env.TWITTER_PREFER_STREAMING_API) {
            while (isActive) {
                val client = createSayaTwitterClient(true) ?: return@coroutineScope

                try {
                    client.use {
                        doStreamLoop(it, keywords)
                    }
                } catch (e: CancellationException) {
                    return@coroutineScope
                } catch (e: ConnectionClosedException) {
                    break
                // レートリミット
                } catch (e: PenicillinException) {
                    break
                } catch (t: Throwable) {
                    logger.error(t) { "error in doStreamLoop" }
                    break
                }

                delay(Duration.seconds(5))
            }
        }

        while (isActive) {
            val client = createSayaTwitterClient() ?: return@coroutineScope

            try {
                client.use {
                    doSearchLoop(it, keywords)
                }
            } catch (e: CancellationException) {
                break
            } catch (t: Throwable) {
                logger.trace(t) { "error in doSearchLoop" }
            }

            delay(Duration.seconds(5))
        }
    }

    private suspend fun doStreamLoop(client: ApiClient, keywords: Set<String>) {
        client.stream.filter(track = keywords.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "connect" }
            }

            override suspend fun onStatus(status: Status) {
                if (status.lang.value != "ja" && status.lang.value != "und") {
                    return
                }

                val comment = status.toSayaComment("Twitter Filter", keywords)
                queue.emit(comment)

                logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "disconnect" }

                if (cause != null && cause !is CancellationException) {
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
                for (status in response.result.statuses.filter { it.lang.value == "ja" || it.lang.value == "und" }) {
                    val comment = status.toSayaComment("Twitter 検索", keywords)
                    queue.emit(comment)

                    logger.trace { "${status.user.name} @${status.user.screenName}: ${status.text}" }
                }
            }

            lastId = response.result.statuses.firstOrNull()?.id?.plus(1) ?: lastId
            val limit = response.rateLimit

            if (limit == null || limit.remaining == 0) {
                delay(Duration.seconds(15))
            } else {
                val duration = JavaDuration.between(Instant.now(), limit.resetAt.toJvmDate().toInstant()).toKotlinDuration()
                val safeRate = duration / limit.remaining
                logger.trace { "Ratelimit ${limit.remaining}/${limit.limit}: Sleep $safeRate ($keywords)" }

                delay(safeRate)
            }
        }
    }
}
