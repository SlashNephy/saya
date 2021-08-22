package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.send
import blue.starry.saya.services.createSayaHttpClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging

class NicoliveSystemWebSocket(private val provider: LiveNicoliveCommentProvider, private val data: EmbeddedData) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${provider.channel.name}]")

    suspend fun start() {
        val client = createSayaHttpClient()
        client.use {
            it.webSocket(data.site.relive.webSocketUrl) {
                logger.debug { "ws:connect" }

                handshake()
                consumeFrames()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handshake() {
        send(
            jsonObjectOf(
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
            )
        )
    }

    private suspend fun DefaultClientWebSocketSession.consumeFrames() {
        var mwsJob: Job? = null
        var keepSeatJob: Job? = null

        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
            val message = frame.readText().let {
                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<NicoliveWebSocketSystemJson>(it)
            }

            when (message.type) {
                "ping" -> {
                    send(
                        jsonObjectOf(
                            "type" to "pong"
                        )
                    )

                    // ログ除外
                    return@collect
                }
                "seat" -> {
                    keepSeatJob?.cancel()
                    val messageData = Json { ignoreUnknownKeys = true }.decodeFromJsonElement<NicoliveWebSocketSystemJsonSeat>(message.data)
                    keepSeatJob = launch {
                        while (isActive) {
                            delay(messageData.keepIntervalSec * 1000)
                            send(
                                jsonObjectOf(
                                    "type" to "keepSeat"
                                )
                            )
                        }
                    }
                }
                "room" -> {
                    mwsJob?.cancel()
                    val messageData = Json { ignoreUnknownKeys = true }.decodeFromJsonElement<NicoliveWebSocketSystemJsonRoom>(message.data)
                    mwsJob = launch {
                        NicoliveMessageWebSocket(provider, messageData, data).start()
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
