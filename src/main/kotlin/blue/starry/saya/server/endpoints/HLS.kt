package blue.starry.saya.server.endpoints

import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunServiceManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.nio.file.Files

private val m3u8 = ContentType("application", "x-mpegURL")

fun Route.getServiceHLS() {
    get("/services/{target}/hls") {
        // Mirakurun の /services/{id} は Service#id であり Service#serviceId ではない！！！
        // ユーザフレンドリーな ServiceId を受け入れる
        val target: Int by call.parameters
        val service = MirakurunServiceManager.findByServiceId(target) ?: return@get call.respond(HttpStatusCode.NotFound)

        val preset = call.parameters["preset"]?.let {
            when (it) {
                "high", "1080p" -> FFMpegWrapper.Preset.High
                "medium", "720p" -> FFMpegWrapper.Preset.Medium
                "low", "360p" -> FFMpegWrapper.Preset.Low
                else -> null
            }
        } ?: FFMpegWrapper.Preset.High

        val playlist = MirakurunStreamManager.openLiveHLS(service, preset)
        call.respond(LocalFileContent(playlist.toFile(), m3u8))
    }

    get("/segments/{filename}") {
        val filename: String by call.parameters
        val path = FFMpegWrapper.TmpDir.resolve(filename)
        if (!Files.exists(path)) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        call.respond(LocalFileContent(path.toFile(), ContentType.Application.OctetStream))
    }
}
