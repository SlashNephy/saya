package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.apache.commons.codec.binary.Base64

fun Route.getLogos() {
    get {
        call.respond(
            MirakurunDataManager.Logos.toList()
        )
    }
}

fun Route.getLogoById() {
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

fun Route.getLogoPngById() {
    get {
        val id: Int by call.parameters
        val logo = MirakurunDataManager.Logos.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName, "logo.png"
        ).toString())

        call.respondBytes(ContentType.Image.PNG) {
            Base64.decodeBase64(logo.data)
        }
    }
}
