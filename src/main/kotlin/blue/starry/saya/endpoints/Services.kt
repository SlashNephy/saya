package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
import blue.starry.saya.respondOrNotFound
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import blue.starry.saya.toBooleanFuzzy
import blue.starry.saya.toFFMpegPreset
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.nio.file.Files

private val m3u8 = ContentType("application", "x-mpegURL")

fun Route.getServicesHLS() {
    get {
        val id: Long by call.parameters
        val service = MirakurunDataManager.Services.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        val preset = call.parameters["preset"].toFFMpegPreset() ?: FFMpegWrapper.Preset.High
        val subTitle = call.parameters["subtitle"].toBooleanFuzzy()

        val playlist = MirakurunStreamManager.openLiveHLS(service, preset, subTitle)

        if (!Files.exists(playlist)) {
            call.respondTextWriter(m3u8) {
                appendLine("#EXTM3U")
                appendLine("#EXT-X-VERSION:3")
                appendLine("#EXT-X-ALLOW-CACHE:YES")
                appendLine("#EXT-X-TARGETDURATION:1")
                appendLine("#EXT-X-MEDIA-SEQUENCE:1")
                appendLine("#EXTINF:1.017000,")
                appendLine("${Env.SAYA_BASE_URI.removeSuffix("/")}/segments/blank.ts")
            }
        } else {
            call.respond(LocalFileContent(playlist.toFile(), m3u8))
        }
    }
}

fun Route.getServices() {
    get {
        call.respond(
            MirakurunDataManager.Services.toList()
        )
    }
}

fun Route.getService() {
    get {
        val id: Long by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Services.find { it.id == id }
        )
    }
}

fun Route.getServicePrograms() {
    get {
        val id: Long by call.parameters

        call.respond(
            MirakurunDataManager.Programs.filter { it.id == id }
        )
    }
}

fun Route.putServices() {
    put {
        MirakurunDataManager.Services.update()
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.getServicesM2TS() {
    get {
        val id: Long by call.parameters
        val service = MirakurunDataManager.Services.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)
        val priority: Int? by call.parameters

        MirakurunApi.getServiceStream(service.id, decode = true, priority = priority).execute {
            val channel = it.receive<ByteReadChannel>()

            call.respondBytesWriter {
                while (!channel.isClosedForRead) {
                    val bytes = channel.toByteArray(512)
                    writeAvailable(bytes)
                }
            }
        }
    }
}

fun Route.getServicesXspf() {
    get {
        val id: Long by call.parameters
        val service = MirakurunDataManager.Services.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondTextWriter(ContentType("application", "xspf+xml")) {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">")
            appendLine("<trackList>")
            appendLine("<track>")
            val url = (Env.SAYA_URL_PREFIX ?: "http://${Env.SAYA_HOST}:${Env.SAYA_PORT}") + "${Env.SAYA_BASE_URI.removeSuffix("/")}/services/${service.id}/m2ts"
            appendLine("<location>$url</location>")
            appendLine("<title>${service.name}</title>")
            appendLine("</track>")
            appendLine("</trackList>")
            appendLine("</playlist>")
        }
    }
}
