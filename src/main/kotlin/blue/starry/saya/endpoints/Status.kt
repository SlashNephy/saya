package blue.starry.saya.endpoints

import blue.starry.saya.models.CommentStatsResponse
import blue.starry.saya.services.CommentStreamManager
import blue.starry.saya.services.comments.isActive
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getCommentStatus() {
    get {
        call.respond(
            CommentStreamManager.Streams.filter {
                it.nico.isActive
            }.map {
                CommentStatsResponse(
                    it.nico?.stats?.provide(),
                    it.twitter.mapNotNull { it.value?.stats?.provide() }.toList()
                )
            }
        )
    }
}
