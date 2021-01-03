package blue.starry.saya.endpoints

import blue.starry.saya.models.Storage
import blue.starry.saya.services.chinachu.ChinachuApi
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getStorageStatus() {
    get {
        val storage = ChinachuApi.getStorage()

        // TODO: directory
        call.respond(
            Storage(
                directory = "TODO: not implemented yet.",
                size = storage.size,
                used = storage.used,
                recorded = storage.recorded,
                available = storage.avail
            )
        )
    }
}
