package blue.starry.saya.endpoints

import blue.starry.saya.services.CommentChannelManager
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getChannels() {
    get {
        call.respond(
            CommentChannelManager.Channels
        )
    }
}
