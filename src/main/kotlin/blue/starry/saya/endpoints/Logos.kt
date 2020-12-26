package blue.starry.saya.endpoints

import blue.starry.saya.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getLogos() {
    get {
        call.respond(
            MirakurunDataManager.Logos.toList()
        )
    }
}

fun Route.getLogo() {
    get {
        val id: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Logos.find { it.id == id }
        )
    }
}

fun Route.putLogos() {
    put {
        MirakurunDataManager.Logos.update()
        call.respond(HttpStatusCode.OK)
    }
}
