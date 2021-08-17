package blue.starry.saya.common

import io.ktor.application.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun ApplicationCall.respondOr404(message: () -> Any?) {
    respond(message() ?: HttpStatusCode.NotFound)
}

suspend inline fun <reified T: Any> DefaultClientWebSocketSession.send(json: T) {
    send(content = Json.encodeToString(json))
}

suspend inline fun DefaultWebSocketSession.rejectWs(message: () -> String) {
    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message()))
}
