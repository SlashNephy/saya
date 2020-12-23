package blue.starry.saya.server.endpoints

import blue.starry.saya.Env
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunServiceManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import blue.starry.saya.toBooleanFuzzy
import blue.starry.saya.toFFMpegPreset
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.nio.file.Files
import java.nio.file.Paths

private val m3u8 = ContentType("application", "x-mpegURL")

fun Route.getServiceHLS() {
    get("/services/{serviceId}/hls") {
        // Mirakurun の /services/{id} は Service#id であり Service#serviceId ではない！！！
        // ユーザフレンドリーな ServiceId を受け入れる
        val serviceId: Int by call.parameters
        val service = MirakurunServiceManager.findByServiceId(serviceId) ?: return@get call.respond(HttpStatusCode.NotFound)

        val preset = call.parameters["preset"].toFFMpegPreset() ?: FFMpegWrapper.Preset.High
        val subTitle = call.parameters["subtitle"].toBooleanFuzzy()

        val playlist = MirakurunStreamManager.openLiveHLS(service, preset, subTitle)

        if (!Files.exists(playlist)) {
            return@get call.respondText(m3u8) {
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
        }

        call.respond(LocalFileContent(playlist.toFile(), m3u8))
    }

    get("/segments/{filename}") {
        val filename: String by call.parameters
        val path = if (filename == "blank.ts") {
            Paths.get("docs", filename)
        } else {
            FFMpegWrapper.TmpDir.resolve(filename)
        }

        if (!Files.exists(path)) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        call.respond(LocalFileContent(path.toFile(), ContentType.Application.OctetStream))
    }
}
