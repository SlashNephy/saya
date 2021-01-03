package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
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

        MirakurunApi.getChannelStream(channel.type.name, channel.group, decode = true, priority = priority).receive { stream: ByteReadChannel ->
            call.respondBytesWriter {
                while (!stream.isClosedForRead) {
                    val byte = stream.readByte()
                    writeByte(byte)
                }
            }
        }
    }
}

fun Route.getChannelsXspfById() {
    get {
        val group: String by call.parameters
        val channel = MirakurunDataManager.Channels.find { it.group == group } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName, "watch.xspf"
        ).toString())

        call.respondTextWriter(ContentType("application", "xspf+xml")) {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">")
            appendLine("<trackList>")
            appendLine("<track>")
            val url = (Env.SAYA_URL_PREFIX ?: "http://${Env.SAYA_HOST}:${Env.SAYA_PORT}") + "${Env.SAYA_BASE_URI.removeSuffix("/")}/channels/${channel.group}/m2ts"
            appendLine("<location>$url</location>")
            appendLine("<title>${channel.name}</title>")
            appendLine("</track>")
            appendLine("</trackList>")
            appendLine("</playlist>")
        }
    }
}

fun Route.getMirakurunChannelsByGroup() {
    get {
        val group: String by call.parameters

        call.respond(
            MirakurunDataManager.Channels.filter {
                it.group == group
            }.map {
                it.json
            }
        )
    }
}

fun Route.getMirakurunChannels() {
    get {
        call.respond(
            MirakurunDataManager.Channels.toList().map { it.json }
        )
    }
}
