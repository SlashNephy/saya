package blue.starry.saya.endpoints

import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

fun Route.getSubscriptions() {
    get {
    }
}

fun Route.getHLSSubscriptions() {
    get {
    }
}

fun Route.getCommentSubscriptions() {
    get {
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
