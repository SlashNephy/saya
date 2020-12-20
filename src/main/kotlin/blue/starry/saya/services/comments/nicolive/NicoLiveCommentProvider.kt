package blue.starry.saya.services.comments.nicolive

import blue.starry.jsonkt.*
import blue.starry.saya.services.comments.Comment
import blue.starry.saya.services.comments.CommentProvider
import blue.starry.saya.services.comments.CommentStream
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketMessageJson
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import blue.starry.saya.services.httpClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging

class NicoLiveCommentProvider(override val stream: CommentStream, private val url: String): CommentProvider {
    private val logger = KotlinLogging.logger("saya.services.nicolive.${stream.id}")

    override val comments = BroadcastChannel<Comment>(1)
    override val stats = NicoLiveStatistics()
    override val job = GlobalScope.launch {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveSystemWebSocket: ${it?.stackTraceToString()}" }
        }
    }

    private suspend fun connect() {
        httpClient.webSocket(url) {
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
                    logger.debug { "コメント勢い: ${stats.commentsPerMinute} コメ/min" }
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
    private val logger = KotlinLogging.logger("saya.services.nicolive.${provider.stream.id}")

    val job = GlobalScope.launch(provider.job) {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveMessageWebSocket: ${it?.stackTraceToString()}" }
        }
    }

    private suspend fun connect() {
        httpClient.webSocket(room.messageServer.uri) {
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
            val comment = message.chat?.let {
                createComment(provider.stream, it)
            }

            if (comment != null) {
                provider.comments.send(comment)
                provider.stats.update(comment)
            }

            logger.trace { message }
        }
    }

    private fun createComment(stream: CommentStream, chat: NicoLiveWebSocketMessageJson.Chat): Comment {
        val (commands, parsed) = parseMail(chat.mail)
        val (color, type, size) = parsed

        return Comment(stream.id, chat.no, chat.date, chat.userId, chat.content, color, type, size, commands)
    }

    private fun parseMail(mail: String): Pair<List<String>, Triple<String, String, String>> {
        val commands = mail.split(' ').filterNot { it == "184" }.filterNot { it.isBlank() }

        var color = "#ffffff"
        var position = "right"
        var size = "normal"
        commands.forEach { command ->
            val c = parseColor(command)
            if (c != null) {
                color = c
            }

            val p = parsePosition(command)
            if (p != null) {
                position = p
            }

            val s = parseSize(command)
            if (s != null) {
                size = s
            }
        }

        return commands to Triple(color, position, size)
    }

    private fun parseColor(command: String): String? {
        return when (command) {
            "red" -> "#e54256"
            "pink" -> "#ff8080"
            "orange" -> "#ffc000"
            "yellow" -> "#ffe133"
            "green" -> "#64dd17"
            "cyan" -> "#39ccff"
            "blue" -> "#0000ff"
            "purple" -> "#d500f9"
            "black" -> "#000000"
            "white" -> "#ffffff"
            "white2" -> "#cccc99"
            "niconicowhite" -> "#cccc99"
            "red2" -> "#cc0033"
            "truered" -> "#cc0033"
            "pink2" -> "#ff33cc"
            "orange2" -> "#ff6600"
            "passionorange" -> "#ff6600"
            "yellow2" -> "#999900"
            "madyellow" -> "#999900"
            "green2" -> "#00cc66"
            "elementalgreen" -> "#00cc66"
            "cyan2" -> "#00cccc"
            "blue2" -> "#3399ff"
            "marineblue" -> "#3399ff"
            "purple2" -> "#6633cc"
            "nobleviolet" -> "#6633cc"
            "black2" -> "#666666"
            else -> null
        }
    }

    private fun parsePosition(command: String): String? {
        return when (command) {
            "ue" -> "top"
            "naka" -> "right"
            "shita" -> "bottom"
            else -> null
        }
    }

    private fun parseSize(command: String): String? {
        if (command == "small" || command == "medium" || command == "big") {
            return command
        }

        return null
    }
}

private suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}
