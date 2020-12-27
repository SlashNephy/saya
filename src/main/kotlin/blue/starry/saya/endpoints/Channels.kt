package blue.starry.saya.endpoints

import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getChannels() {
    get {
        call.respond(
            MirakurunDataManager.Channels.toList()
        )
    }
}

fun Route.getChannelsByGroup() {
    get {
        val group: String by call.parameters

        call.respond(
            MirakurunDataManager.Channels.filter {
                it.group == group
            }
        )
    }
}

fun Route.putChannels() {
    put {
        MirakurunDataManager.Channels.update()
        call.respond(HttpStatusCode.OK)
    }
}
