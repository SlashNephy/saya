package blue.starry.saya.endpoints

import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*

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

fun Route.getChannelsM2TSByGroup() {
    get {
        val group: String by call.parameters
        val channel = MirakurunDataManager.Channels.find { it.group == group } ?: return@get call.respond(HttpStatusCode.NotFound)
        val priority = call.parameters["priority"]?.toIntOrNull()

        MirakurunApi.getChannelStream(channel.type.name, channel.group, decode = true, priority = priority).receive { channel: ByteReadChannel ->
            call.respondBytesWriter {
                while (!channel.isClosedForRead) {
                    val byte = channel.readByte()
                    writeByte(byte)
                }
            }
        }
    }
}
