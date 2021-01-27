package blue.starry.saya.endpoints

import blue.starry.saya.services.CommentStreamManager.Streams
import blue.starry.saya.services.comments.withSession
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.nio.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.wsServiceCommentsById() {
    webSocket {
        val id: Long by call.parameters
        val service = MirakurunDataManager.Services.find {
            it.id == id
        } ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Service $id is not found."))
        val stream = Streams.find {
            it.channel.serviceIds.contains(service.actualId)
        } ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Service $id is not found."))

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
