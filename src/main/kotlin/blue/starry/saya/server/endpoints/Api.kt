package blue.starry.saya.server.endpoints

import blue.starry.saya.services.nicolive.streams
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.getCommentStream() {
    webSocket("/comment/stream/{target}") {
        val target = call.parameters["target"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val stream = streams.find {
            it.id == target
        }
        val socket = stream?.getOrCreateSocket() ?: return@webSocket call.respond(HttpStatusCode.NotFound)

        try {
            stream.addSubscriber()

            // タイムアウト 10s
            withTimeout(10000) {
                while (socket.messageSocket == null) {
                    delay(100)
                }
            }

            socket.messageSocket!!.stream.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }
        } finally {
            stream.removeSubscriber()
        }
    }
}

fun Route.getCommentStats() {
    get("/comment/stats/{target}") {
        val target = call.parameters["target"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val stream = streams.find {
            it.id == target
        }
        val socket = stream?.getOrCreateSocket(false) ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondText {
            Json.encodeToString(socket.stats)
        }
    }
}
