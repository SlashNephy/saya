package blue.starry.saya.endpoints

import blue.starry.saya.services.CommentChannelManager
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getChannelDefinitions() {
    get {
        call.respond(
            CommentChannelManager.Channels
        )
    }
}

fun Route.getBoardDefinitions() {
    get {
        call.respond(
            CommentChannelManager.Boards
        )
    }
}
