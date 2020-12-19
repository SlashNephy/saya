package blue.starry.saya.server.endpoints

import blue.starry.jsonkt.encodeToString
import blue.starry.saya.services.nicolive.streams
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay

fun Route.getCommentsStream() {
    webSocket("/comments/stream/{target}") {
        val target = call.parameters["target"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val stream = streams.find {
            it.id == target
        }
        val socket = stream?.getOrCreateSocket() ?: return@webSocket call.respond(HttpStatusCode.NotFound)

        try {
            stream.addSubscriber()

            while (socket.messageSocket == null) {
                delay(100)
            }

            socket.messageSocket!!.stream.openSubscription().consumeEach {
                send(it.json.encodeToString())
            }
        } finally {
            stream.removeSubscriber()
        }
    }
}
