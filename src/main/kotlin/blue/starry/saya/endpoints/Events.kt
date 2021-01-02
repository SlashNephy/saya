package blue.starry.saya.endpoints

import blue.starry.saya.services.EventStreamManager
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.wsEventStream() {
    webSocket {
        for (event in EventStreamManager.enumerate()) {
            send(Json.encodeToString(event))
        }

        EventStreamManager.Stream.openSubscription().consumeEach {
            send(Json.encodeToString(it))
        }
    }
}
