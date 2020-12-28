package blue.starry.saya.endpoints

import blue.starry.saya.models.CommentStatsResponse
import blue.starry.saya.services.comments.CommentStreamManager
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
        val target: String by call.parameters
        val stream = CommentStreamManager.findBy(target)
        val nicoLive = stream?.getOrCreateNicoLiveProvider() ?: return@webSocket call.respond(HttpStatusCode.NotFound)

//        val hashtag = call.parameters["hashtag"]
//        val sample = call.parameters["sample"] == "1"
//        val twitter = if (sample) {
//            stream.getOrCreateTwitterProvider(null)
//        } else {
//            hashtag?.let { stream.getOrCreateTwitterProvider(it) }
//        }

        nicoLive.withSession {
            nicoLive.comments.openSubscription().consumeEach {
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
        val target: String by call.parameters
        val stream = CommentStreamManager.findBy(target)

        call.respond(
            CommentStatsResponse(stream?.nico?.stats?.provide(), stream?.twitter?.mapNotNull { it.value?.stats?.provide() }?.toList().orEmpty())
        )
    }
}
