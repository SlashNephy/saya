package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOr404
import blue.starry.saya.services.SayaMirakcAribWrapper
import blue.starry.saya.services.mirakc.MountPointManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getFiles() {
    get {
        call.respond(
            MountPointManager.Files.toList()
        )
    }
}

fun Route.getFileById() {
    get {
        val id: String by call.parameters

        call.respondOr404 {
            MountPointManager.Files.find { it.id == id }
        }
    }
}

fun Route.getFileInfoById() {
    get {
        val id: String by call.parameters

        call.respondOr404 {
            val file = MountPointManager.Files.find { it.id == id } ?: return@respondOr404 null

            SayaMirakcAribWrapper?.detectOnAir(file, true)
        }
    }
}
