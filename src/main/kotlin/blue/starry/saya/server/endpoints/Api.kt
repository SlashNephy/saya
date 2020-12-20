package blue.starry.saya.server.endpoints

import blue.starry.saya.services.comments.CommentStreamManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.getCommentStream() {
    webSocket("/comment/stream/{target}") {
        val stream = CommentStreamManager.findBy(call.parameters["target"]!!)
        val provider = stream?.getNicoLiveCommentProvider() ?: return@webSocket call.respond(HttpStatusCode.NotFound)

        stream.withSession {
            provider.comments.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }
        }
    }
}

fun Route.getCommentStats() {
    get("/comment/stats/{target}") {
        val stream = CommentStreamManager.findBy(call.parameters["target"]!!)
        val provider = stream?.getNicoLiveCommentProvider(false) ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondText {
            Json.encodeToString(provider.stats)
        }
    }
}
