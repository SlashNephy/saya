package blue.starry.saya.services.comments

import blue.starry.jsonkt.*
import blue.starry.saya.services.SayaHttpClient
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketMessageJson
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import blue.starry.saya.services.nicolive.toSayaComment
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

class NicoLiveCommentProvider(override val stream: CommentStream, private val url: String, val source: String): CommentProvider {
    private val logger = KotlinLogging.logger("saya.services.nicolive.${stream.channel.name}")

    override val subscriptions = AtomicInteger(0)
    override val stats = NicoLiveStatisticsProvider(source)
    override val job = GlobalScope.launch {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveSystemWebSocket: ${it?.stackTraceToString()}" }
        }
    }

    private suspend fun connect() {
        SayaHttpClient.webSocket(url) {
            try {
                logger.debug { "ws:connect" }

                handshake()
                consumeFrames()
            } catch (t: Throwable) {
                when (t) {
                    is ClosedReceiveChannelException, is CancellationException -> {
                        logger.debug("ws:close (${closeReason.await()})")
                    }
                    else -> {
                        logger.error("ws:error (${closeReason.await()}), ${t.stackTraceToString()}")
                    }
                }
            } finally {
                job.cancel()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handshake() {
        send(jsonObjectOf(
            "type" to "startWatching",
            "data" to jsonObjectOf(
                "stream" to jsonObjectOf(
                    "quality" to "high",
                    "protocol" to "hls",
                    "latency" to "high",
                    "chasePlay" to false
                ),
                "room" to jsonObjectOf(
                    "protocol" to "webSocket",
                    "commentable" to true
                ),
                "reconnect" to false
            )
        ))
    }

    private suspend fun DefaultClientWebSocketSession.consumeFrames() {
        var mws: NicoLiveMessageWebSocket? = null
        var keepSeatJob: Job? = null

        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
            val message = frame.readText().parseObject {
                NicoLiveWebSocketSystemJson(it)
            }

            when (message.type) {
                "ping" -> {
                    send(jsonObjectOf(
                        "type" to "pong"
                    ))

                    // ログ除外
                    return@collect
                }
                "seat" -> {
                    keepSeatJob?.cancel()
                    keepSeatJob = launch(job) {
                        while (isActive) {
                            delay(message.data.keepIntervalSec * 1000)
                            send(jsonObjectOf(
                                "type" to "keepSeat"
                            ))
                        }
                    }
                }
                "room" -> {
                    mws?.job?.cancel()
                    mws = NicoLiveMessageWebSocket(this@NicoLiveCommentProvider, message.data)
                }
                // 1分おきに来る
                "statistics" -> {
                    stats.update(message.data)
                    logger.debug { "コメント勢い: ${stats.provide().commentsPerMinute} コメ/min" }
                }
                "disconnect" -> {
                    job.cancel()
                }
            }

            logger.trace { message }
        }
    }
}

private class NicoLiveMessageWebSocket(private val provider: NicoLiveCommentProvider, private val room: NicoLiveWebSocketSystemJson.Data) {
    private val logger = KotlinLogging.logger("saya.services.nicolive.${provider.stream.channel.name}")

    val job = GlobalScope.launch(provider.job) {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveMessageWebSocket: ${it?.stackTraceToString()}" }
            provider.job.cancel()
        }
    }

    private suspend fun connect() {
        SayaHttpClient.webSocket(room.messageServer.uri) {
            try {
                logger.debug { "mws:connect" }

                handshake()
                consumeFrames()
            } catch (t: Throwable) {
                when (t) {
                    is ClosedReceiveChannelException, is CancellationException -> {
                        logger.debug("mws:close (${closeReason.await()})")
                    }
                    else -> {
                        logger.error("mws:error (${closeReason.await()}), ${t.stackTraceToString()}")
                    }
                }
            } finally {
                job.cancel()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handshake() {
        send(jsonArrayOf(
            jsonObjectOf(
                "ping" to jsonObjectOf(
                    "content" to "rs:0"
                )
            ),
            jsonObjectOf(
                "ping" to jsonObjectOf(
                    "content" to "ps:0"
                )
            ),
            jsonObjectOf(
                "thread" to jsonObjectOf(
                    "thread" to room.threadId,
                    "version" to "20061206",
                    "user_id" to "guest",
                    "res_from" to 0,
                    "with_global" to 1,
                    "scores" to 1,
                    "nicoru" to 0
                )
            ),
            jsonObjectOf(
                "ping" to jsonObjectOf(
                    "content" to "pf:0"
                )
            ),
            jsonObjectOf(
                "ping" to jsonObjectOf(
                    "content" to "rf:0"
                )
            )
        ))
    }

    private suspend fun DefaultClientWebSocketSession.consumeFrames() {
        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
            val message = frame.readText().parseObject {
                NicoLiveWebSocketMessageJson(it)
            }
            val comment = message.chat?.toSayaComment(provider.source)

            if (comment != null && !comment.text.startsWith("/")) {
                provider.stream.comments.send(comment)
                provider.stats.update(comment)
            }

            logger.trace { message }
        }
    }
}

private suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}
