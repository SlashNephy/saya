package blue.starry.saya.server.endpoints

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File

fun Route.getIndex() {
    get("/") {
        call.respondFile(File("resources"), "index.html")
    }
}
