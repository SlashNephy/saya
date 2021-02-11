package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.jsonkt.parseObject
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.send
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging

class NicoliveSystemWebSocket(private val provider: LiveNicoliveCommentProvider, private val data: EmbeddedData) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${provider.channel.name}]")

    suspend fun start() {
        SayaHttpClient.webSocket(data.site.relive.webSocketUrl) {
            logger.debug { "ws:connect" }

            handshake()
            consumeFrames()
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
            val message = frame.readText().parseObject {
                NicoliveWebSocketSystemJson(it)
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
                    keepSeatJob = launch {
                        while (isActive) {
                            delay(message.data.keepIntervalSec * 1000)
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
                    mwsJob = launch {
                        NicoliveMessageWebSocket(provider, message.data, data).start()
                    }.apply {
                        invokeOnCompletion {
                            runBlocking {
                                close()
                            }
                        }
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
