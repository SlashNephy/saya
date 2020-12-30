package blue.starry.saya.endpoints

import blue.starry.saya.common.send
import blue.starry.saya.services.EventStreamManager
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.wsEventStream() {
    webSocket {
        for (event in EventStreamManager.enumerate()) {
            send(event)
        }

        EventStreamManager.Stream.openSubscription().consumeEach {
            send(it)
        }
    }
}
