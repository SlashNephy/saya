package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.*
import blue.starry.saya.services.httpClient
import blue.starry.saya.services.nicolive.models.Channel
import blue.starry.saya.services.nicolive.models.NicoLiveWebSocketMessageJson
import blue.starry.saya.services.nicolive.models.NicoLiveWebSocketSystemJson
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mu.KotlinLogging

data class Stream(val id: String, val channel: Channel) {
    private var underlyingSocket: NicoLiveSystemWebSocket? = null

    suspend fun getOrCreateSocket(creating: Boolean = true): NicoLiveSystemWebSocket? {
        // タグなし
        if (channel.tags.isEmpty()) {
            return null
        }

        val soc = underlyingSocket
        if (soc != null && soc.job.isActive) {
            return soc
        }

        if (!creating) {
            return null
        }

        val data = channel.tags.flatMap { tag ->
            NicoLiveApi.getLivePrograms(tag).data
                .filter { data ->
                    // 公式番組優先
                    !channel.official || data.tags.any { it.text == "ニコニコ実況" }
                }
                .map {
                    "https://live2.nicovideo.jp/watch/${it.id}"
                }.map {
                    NicoLiveApi.getEmbeddedData(it)
                }
        }.firstOrNull() ?: return null

        underlyingSocket = NicoLiveSystemWebSocket(this, data.site.relive.webSocketUrl)
        return underlyingSocket
    }

    fun closeSocket() {
        underlyingSocket?.job?.cancel()
        underlyingSocket = null
    }

    private var subscribers = 0

    fun addSubscriber() {
        subscribers++
    }

    fun removeSubscriber() {
        if (--subscribers == 0) {
            closeSocket()
        }
    }
}

class NicoLiveSystemWebSocket(val stream: Stream, private val url: String) {
    private val logger = KotlinLogging.logger("saya.services.nicolive.${stream.id}")
    val job = GlobalScope.launch {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveSystemWebSocket: ${it?.stackTraceToString()}" }
        }
    }

    var messageSocket: NicoLiveMessageWebSocket? = null
        private set
    private var keepSeatJob: Job? = null

    val stats = NicoLiveStatistics()

    private suspend fun connect() {
        httpClient.webSocket(url) {
            try {
                logger.debug("ws:connect")

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
        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
            val message = frame.readText().parseObject { NicoLiveWebSocketSystemJson(it) }

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
                    messageSocket?.job?.cancel()
                    messageSocket = NicoLiveMessageWebSocket(this@NicoLiveSystemWebSocket, message)
                }
                // 1分おきに来る
                "statistics" -> {
                    stats.update(message.data)
                    logger.debug { "コメント勢い: ${stats.commentRate} コメ/min" }
                }
                "disconnect" -> {
                    job.cancel()
                }
            }

            logger.trace { message }
        }
    }
}

@Serializable
data class NicoLiveStatistics(
    var viewers: Int? = null,
    var adPoints: Int? = null,
    var giftPoints: Int? = null,
    var comments: Int? = null,
    var commentRate: Int? = null
) {
    @Transient
    private var commentsTime: Int? = null
    @Transient
    private var firstComments: Int? = null
    @Transient
    private var firstCommentsTime: Int? = null

    fun update(data: NicoLiveWebSocketSystemJson.Data) {
        viewers = data.viewers
        adPoints = data.adPoints
        giftPoints = data.giftPoints
    }

    // stats の精度がよくないのでコメントの no から計算
    fun update(comment: Comment) {
        if (firstComments == null) {
            firstComments = comment.no
        }
        if (firstCommentsTime == null) {
            firstCommentsTime = comment.time
        }

        comments = comment.no
        commentsTime = comment.time

        val c = comments ?: return
        val fc = firstComments ?: return
        val ct = commentsTime ?: return
        val fct = firstCommentsTime ?: return
        if (ct == fct) {
            return
        }

        commentRate = 60 * (c - fc) / (ct - fct)
    }
}

class NicoLiveMessageWebSocket(private val system: NicoLiveSystemWebSocket, private val room: NicoLiveWebSocketSystemJson) {
    private val logger = KotlinLogging.logger("saya.services.nicolive.${system.stream.id}")
    val comments = BroadcastChannel<Comment>(1)

    val job = GlobalScope.launch(system.job) {
        connect()
    }.apply {
        invokeOnCompletion {
            logger.trace { "cancel: NicoLiveMessageWebSocket: ${it?.stackTraceToString()}" }
        }
    }

    private suspend fun connect() {
        httpClient.webSocket(room.data.messageServer.uri) {
            try {
                logger.debug("mws:connect")

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
                    "thread" to room.data.threadId,
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
                Comment.from(system.stream, it)
            }

            if (comment != null) {
                comments.send(comment)
                system.stats.update(comment)
            }

            logger.trace { message }
        }
    }
}

private suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}

@Serializable
data class Comment(val channel: String, val no: Int, val time: Int, val author: String, val text: String, val color: String, val type: String, val commands: List<String>) {
    companion object {
        fun from(stream: Stream, chat: NicoLiveWebSocketMessageJson.Chat): Comment {
            val (commands, color, type) = parseMail(chat.mail.orEmpty())

            return Comment(stream.id, chat.no, chat.date, chat.userId, chat.content, color, type, commands)
        }

        private fun parseMail(mail: String): Triple<List<String>, String, String> {
            val commands = mail.split(' ').filter { it != "184" }

            var color = "#ffffff"
            var position = "right"
            commands.forEach { command ->
                val c = parseColor(command)
                if (c != null) {
                    color = c
                }

                val p = parsePosition(command)
                if (p != null) {
                    position = p
                }
            }

            return Triple(commands, color, position)
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
    }
}
