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
import mu.KotlinLogging

data class Stream(val id: String, val channel: Channel) {
    private var underlyingSocket: NicoLiveSystemWebSocket? = null

    suspend fun getOrCreateSocket(): NicoLiveSystemWebSocket? {
        // タグなし
        if (channel.tags.isEmpty()) {
            return null
        }

        val soc = underlyingSocket
        if (soc != null && soc.job.isActive) {
            return soc
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

        underlyingSocket = NicoLiveSystemWebSocket(data.site.relive.webSocketUrl)
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

class NicoLiveSystemWebSocket(private val url: String) {
    private val logger = KotlinLogging.logger("saya.services.nicolive")
    val job = GlobalScope.launch {
        connect()
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
            } catch (_: ClosedReceiveChannelException) {
                logger.debug("ws:close (${closeReason.await()})")
            } catch (t: Throwable) {
                logger.error("ws:error (${closeReason.await()}), ${t.stackTraceToString()}")
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
                }
                "disconnect" -> {
                    job.cancel()
                }
            }

            logger.trace { message }
        }
    }
}

class NicoLiveStatistics {
    private val logger = KotlinLogging.logger("saya.services.nicolive")

    var viewers: Int? = null
        private set
    var adPoints: Int? = null
        private set
    var giftPoints: Int? = null
        private set

    var comments: Int? = null
        private set
    private var commentsTime: Int? = null
    private var firstComments: Int? = null
    private var firstCommentsTime: Int? = null

    val commentsPerMin: Int?
        get() {
            val c = comments ?: return null
            val fc = firstComments ?: return null
            val ct = commentsTime ?: return null
            val fct = firstCommentsTime ?: return null
            if (ct == fct) {
                return null
            }

            return 60 * (c - fc) / (ct - fct)
        }

    fun update(data: NicoLiveWebSocketSystemJson.Data) {
        viewers = data.viewers
        adPoints = data.adPoints
        giftPoints = data.giftPoints

        logger.debug { "コメント勢い: $commentsPerMin コメ/min" }
    }

    // stats の精度がよくないのでコメントの no から計算
    fun update(chat: NicoLiveWebSocketMessageJson.Chat) {
        if (firstComments == null) {
            firstComments = chat.no
        }
        if (firstCommentsTime == null) {
            firstCommentsTime = chat.date
        }

        comments = chat.no
        commentsTime = chat.date
    }
}

class NicoLiveMessageWebSocket(private val system: NicoLiveSystemWebSocket, private val room: NicoLiveWebSocketSystemJson) {
    private val logger = KotlinLogging.logger("saya.services.nicolive")
    val stream = BroadcastChannel<NicoLiveWebSocketMessageJson.Chat>(1)

    val job = GlobalScope.launch(system.job) {
        connect()
    }

    private suspend fun connect() {
        httpClient.webSocket(room.data.messageServer.uri) {
            try {
                logger.debug("mws:connect")

                handshake()
                consumeFrames()
            } catch (_: ClosedReceiveChannelException) {
                logger.debug("mws:close (${closeReason.await()})")
            } catch (t: Throwable) {
                logger.error("mws:error (${closeReason.await()}), ${t.stackTraceToString()}")
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
            val message = frame.readText().parseObject { NicoLiveWebSocketMessageJson(it) }

            message.chat?.also {
                stream.send(it)
                system.stats.update(it)
            }

            logger.trace { message }
        }
    }
}

private suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}
