package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getPrograms() {
    get {
        call.respond(
            MirakurunDataManager.Programs.toList()
        )
    }
}

fun Route.getProgram() {
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
