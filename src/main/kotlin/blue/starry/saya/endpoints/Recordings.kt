package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.chinachu.ChinachuApi
import blue.starry.saya.services.chinachu.ChinachuDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getRecordings() {
    get {
        call.respond(
            ChinachuDataManager.Recordings.toList()
        )
    }
}

fun Route.getRecording() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            ChinachuDataManager.Recordings.find { it.program.id == id }
        )
    }
}

fun Route.deleteRecording() {
    get {
        val id: Long by call.parameters

        ChinachuApi.deleteRecording(id.toString(36))
        call.respond(HttpStatusCode.OK)
    }
}

