package blue.starry.saya.endpoints

import blue.starry.saya.models.CommentStatsResponse
import blue.starry.saya.services.CommentStreamManager
import blue.starry.saya.services.comments.withSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.wsCommentStream() {
    webSocket {
        val id: Int by call.parameters
        val stream = CommentStreamManager.findByServiceId(id)
            ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Service $id is not found."))

//        val hashtag = call.parameters["hashtag"]
//        val sample = call.parameters["sample"] == "1"
//        val twitter = if (sample) {
//            stream.getOrCreateTwitterProvider(null)
//        } else {
//            hashtag?.let { stream.getOrCreateTwitterProvider(it) }
//        }

        stream.getOrCreateNicoLiveProvider().withSession {
            stream.comments.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }

//            twitter.withSession {
//                (twitter.comments.openSubscription() + nicoLive.comments.openSubscription())
//                nicoLive.comments.openSubscription().consumeEach {
//                    send(Json.encodeToString(it))
//                }
//            }
        }
    }
}

fun Route.getCommentStats() {
    get {
        val id: Int by call.parameters
        val stream = CommentStreamManager.findByServiceId(id) ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(
            CommentStatsResponse(
                stream.nico?.stats?.provide(),
                stream.twitter.mapNotNull { it.value?.stats?.provide() }.toList()
            )
        )
    }
}
