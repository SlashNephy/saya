package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.*
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.CommentProvider
import blue.starry.saya.services.SayaHttpClient
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketMessageJson
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging

class NicoLiveCommentProvider(
    override val channel: JikkyoChannel,
    override val comments: BroadcastChannel<Comment>,
    private val wsUrl: String,
    val lvName: String
): CommentProvider {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive.${channel.name}")

    suspend fun start() {
        try {
            connect()
        } catch (t: Throwable) {
            logger.trace(t) { "cancel: NicoLiveSystemWebSocket" }
        }
    }

    private suspend fun connect() {
        SayaHttpClient.webSocket(wsUrl) {
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
        var mwsJob: Job? = null
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
                    keepSeatJob = launch {
                        while (isActive) {
                            delay(message.data.keepIntervalSec * 1000)
                            send(jsonObjectOf(
                                "type" to "keepSeat"
                            ))
                        }
                    }
                }
                "room" -> {
                    mwsJob?.cancel()
                    mwsJob = launch {
                        NicoLiveMessageWebSocket(this@NicoLiveCommentProvider, message.data).start()
                    }
                }
                "disconnect" -> {
                    close()
                }
            }

            logger.trace { message }
        }
    }
}

private class NicoLiveMessageWebSocket(private val provider: NicoLiveCommentProvider, private val room: NicoLiveWebSocketSystemJson.Data) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive.${provider.channel.name}")

    suspend fun start() {
        try {
            connect()
        } catch (t: Throwable) {
            logger.trace(t) { "cancel: NicoLiveMessageWebSocket" }
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
            val comment = message.chat?.toSayaComment(provider.lvName)

            if (comment != null && !comment.text.startsWith("/")) {
                provider.comments.send(comment)
            }

            logger.trace { message }
        }
    }
}

private suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}
