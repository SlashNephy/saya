package blue.starry.saya.endpoints

import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
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

fun Route.getChannelsByType() {
    get {
        val type: String by call.parameters

        call.respond(
            MirakurunDataManager.Channels.filter {
                it.type.name.equals(type, true)
            }
        )
    }
}

fun Route.getChannelsByTypeAndGroup() {
    get {
        val type: String by call.parameters
        val group: String by call.parameters

        call.respond(
            MirakurunDataManager.Channels.filter {
                it.type.name.equals(type, true) && it.group == group
            }
        )
    }
}

fun Route.getChannelServicesByTypeAndGroup() {
    get {
        val type: String by call.parameters
        val group: String by call.parameters

        call.respond(
            MirakurunDataManager.Channels.filter {
                it.type.name.equals(type, true) && it.group == group
            }
        )
    }
}
