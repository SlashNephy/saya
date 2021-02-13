package blue.starry.saya.common

import blue.starry.jsonkt.JsonElement
import blue.starry.jsonkt.encodeToString
import io.ktor.application.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*

suspend inline fun ApplicationCall.respondOr404(message: () -> Any?) {
    respond(message() ?: HttpStatusCode.NotFound)
}

suspend fun DefaultClientWebSocketSession.send(json: JsonElement) {
    send(json.encodeToString())
}

suspend inline fun DefaultWebSocketSession.rejectWs(message: () -> String) {
    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message()))
}
