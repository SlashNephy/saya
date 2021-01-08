package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*

fun Route.getPrograms() {
    get {
        call.respond(
            MirakurunDataManager.Programs.toList()
        )
    }
}

fun Route.getProgramById() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Programs.find { it.id == id }
        )
    }
}

fun Route.putPrograms() {
    put {
        MirakurunDataManager.Programs.update()
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.getProgramM2TSById() {
    get {
        val id: Long by call.parameters
        val program = MirakurunDataManager.Programs.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)
        val priority = call.parameters["priority"]?.toIntOrNull()

        MirakurunApi.getProgramStream(program.id, decode = true, priority = priority).receive { channel: ByteReadChannel ->
            call.respondBytesWriter {
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(Env.SAYA_M2TS_BUFFERSIZE)
                    writePacket(packet)
                }
            }
        }
    }
}

fun Route.getProgramXspfById() {
    get {
        val id: Long by call.parameters
        val program = MirakurunDataManager.Programs.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName, "watch.xspf"
        ).toString())

        call.respondTextWriter(ContentType("application", "xspf+xml")) {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">")
            appendLine("<trackList>")
            appendLine("<track>")
            val url = (Env.SAYA_URL_PREFIX ?: "http://${Env.SAYA_HOST}:${Env.SAYA_PORT}") + "${Env.SAYA_BASE_URI.removeSuffix("/")}/programs/${program.id}/m2ts"
            appendLine("<location>$url</location>")
            appendLine("<title>${program.name}</title>")
            appendLine("</track>")
            appendLine("</trackList>")
            appendLine("</playlist>")
        }
    }
}

fun Route.getMirakurunProgramById() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Programs.find { it.id == id }?.json
        )
    }
}

fun Route.getMirakurunPrograms() {
    get {
        call.respond(
            MirakurunDataManager.Programs.toList().map { it.json }
        )
    }
}

