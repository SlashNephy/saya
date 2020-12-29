package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.models.RecordedFile
import blue.starry.saya.services.chinachu.ChinachuApi
import blue.starry.saya.services.chinachu.ChinachuDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.nio.file.Files
import java.nio.file.Paths

fun Route.getRecords() {
    get {
        call.respond(
            ChinachuDataManager.Recorded.toList()
        )
    }
}

fun Route.getRecord() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            ChinachuDataManager.Recorded.find { it.program.id == id }
        )
    }
}

fun Route.deleteRecord() {
    delete {
        val id: Long by call.parameters

        ChinachuApi.deleteRecorded(id.toString(36))
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.getRecordFile() {
    get {
        val id: Long by call.parameters

        val record = ChinachuDataManager.Recorded.find { it.program.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)
        val path = Paths.get(record.path)

        if (Files.exists(path)) {
            call.respond(
                RecordedFile(
                    path = record.path,
                    size = Files.size(path)
                )
            )
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun Route.deleteRecordFile() {
    delete {
        val id: Long by call.parameters

        val record = ChinachuDataManager.Recorded.find { it.program.id == id } ?: return@delete call.respond(HttpStatusCode.NotFound)
        val path = Paths.get(record.path)

        Files.delete(path)
        call.respond(HttpStatusCode.OK)
    }
}
