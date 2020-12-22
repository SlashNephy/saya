package blue.starry.saya.server.endpoints

import blue.starry.saya.services.comments.CommentStreamManager
import blue.starry.saya.services.comments.nicolive.NicoLiveStatisticsProvider
import blue.starry.saya.services.comments.twitter.TwitterHashTagStatistics
import blue.starry.saya.services.comments.withSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.getCommentStream() {
    webSocket("/comments/{target}/stream") {
        val target = call.parameters.getOrFail("target")
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
    get("/comments/{target}/stats") {
        val target = call.parameters.getOrFail("target")
        val stream = CommentStreamManager.findBy(target)

        call.respondText {
            Json.encodeToString(
                CommentStatsResponse(stream?.nico?.stats?.provide(), stream?.twitter?.map { it.key to it.value?.stats?.provide() }?.toMap())
            )
        }
    }
}

@Serializable data class CommentStatsResponse(
    val nico: NicoLiveStatisticsProvider.Statistics?,
    val twitter: Map<String, TwitterHashTagStatistics.Statistics?>?,
)
