package blue.starry.saya.endpoints

import blue.starry.saya.models.CommentStatsResponse
import blue.starry.saya.services.CommentStreamManager
import blue.starry.saya.services.comments.isActive
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

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

fun Route.getCommentStatusById() {
    get {
        val id: Long by call.parameters
        val service = MirakurunDataManager.Services.find {
            it.id == id
        } ?: return@get call.respond(HttpStatusCode.NotFound)
        val stream = CommentStreamManager.Streams.find {
            it.channel.serviceIds.contains(service.actualId)
        } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(
            CommentStatsResponse(
                stream.nico?.stats?.provide(),
                stream.twitter.mapNotNull { it.value?.stats?.provide() }.toList()
            )
        )
    }
}
