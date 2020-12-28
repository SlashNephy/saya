package blue.starry.saya.endpoints

import blue.starry.saya.services.comments.CommentStreamManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

fun Route.getSubscriptions() {
    get {
        call.respond(
            MirakurunStreamManager.getSubscriptions() + CommentStreamManager.getSubscriptions()
        )
    }
}

fun Route.getHLSSubscriptions() {
    get {
        call.respond(
            MirakurunStreamManager.getSubscriptions()
        )
    }
}

fun Route.getCommentSubscriptions() {
    get {
        call.respond(
            CommentStreamManager.getSubscriptions()
        )
    }
}

fun Route.getEventSubscriptions() {
    get {

    }
}

@Serializable data class Subscription(
    val id: String,
    val type: Type,
    val subscribers: Int
) {
    enum class Type {
        HLS, Comments, Events
    }
}
