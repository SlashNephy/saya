package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.chinachu.ChinachuApi
import blue.starry.saya.services.chinachu.ChinachuDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getReserves() {
    get {
        call.respond(
            ChinachuDataManager.Reserves.toList()
        )
    }
}

fun Route.getReserve() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            ChinachuDataManager.Reserves.find { it.program.id == id }
        )
    }
}

fun Route.deleteReserve() {
    get {
        val id: Long by call.parameters

        ChinachuApi.deleteReserve(id.toString(36))
        call.respond(HttpStatusCode.OK)
    }
}
