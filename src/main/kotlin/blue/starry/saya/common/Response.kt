package blue.starry.saya.common

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend inline fun ApplicationCall.respondOr404(message: () -> Any?) {
    respond(message() ?: HttpStatusCode.NotFound)
}
