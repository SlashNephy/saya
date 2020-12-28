package blue.starry.saya.endpoints

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
                    val byte = channel.readByte()
                    writeByte(byte)
                }
            }
        }
    }
}
