package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.jsonArrayOf
import blue.starry.jsonkt.jsonObjectOf
import blue.starry.jsonkt.parseObject
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.send
import blue.starry.saya.services.createSayaHttpClient
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging

class NicoliveMessageWebSocket(
    private val provider: LiveNicoliveCommentProvider,
    private val room: NicoliveWebSocketSystemJson.Data,
    private val data: EmbeddedData
) {
    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${provider.channel.name}]")

    suspend fun start() {
        createSayaHttpClient().webSocket(room.messageServer.uri) {
            logger.debug { "mws:connect" }

            handshake()
            consumeFrames()
        }
    }

    private suspend fun DefaultClientWebSocketSession.handshake() {
        send(
            jsonArrayOf(
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
            )
        )
    }

    private suspend fun DefaultClientWebSocketSession.consumeFrames() {
        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect { frame ->
            val message = frame.readText().parseObject {
                NicoliveWebSocketMessageJson(it)
            }

            val comment = message.chat?.toSayaComment(
                source = "ニコニコ生放送 [${data.program.title}]",
                sourceUrl = "https://live2.nicovideo.jp/watch/${data.program.nicoliveProgramId}"
            )
            if (comment != null) {
                provider.queue.send(comment)
            }

            logger.trace { message }
        }
    }
}
