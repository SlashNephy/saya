package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
import blue.starry.saya.respondOrNotFound
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import blue.starry.saya.toBooleanFuzzy
import blue.starry.saya.toFFMpegPreset
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import java.nio.file.Files

private val m3u8 = ContentType("application", "x-mpegURL")

fun Route.getServicesHLS() {
    get {
        // Mirakurun の /services/{id} は Service#id であり Service#serviceId ではない！！！
        // ユーザフレンドリーな ServiceId を受け入れる
        val id: Int by call.parameters
        val service = MirakurunDataManager.Services.find { it.serviceId == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        val preset = call.parameters["preset"].toFFMpegPreset() ?: FFMpegWrapper.Preset.High
        val subTitle = call.parameters["subtitle"].toBooleanFuzzy()

        val playlist = MirakurunStreamManager.openLiveHLS(service, preset, subTitle)

        if (!Files.exists(playlist)) {
            call.respondText(m3u8) {
                """
                #EXTM3U
                #EXT-X-VERSION:3
                #EXT-X-ALLOW-CACHE:YES
                #EXT-X-TARGETDURATION:1
                #EXT-X-MEDIA-SEQUENCE:1
                #EXTINF:1.017000,
                ${Env.SAYA_BASE_URI.removeSuffix("/")}/segments/blank.ts
                """.trimIndent()
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
        val id: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Services.find { it.serviceId == id }
        )
    }
}

fun Route.getServicePrograms() {
    get {
        val id: Int by call.parameters

        call.respond(
            MirakurunDataManager.Programs.filter { it.serviceId == id }
        )
    }
}

fun Route.putServices() {
    put {
        MirakurunDataManager.Services.update()
        call.respond(HttpStatusCode.OK)
    }
}
