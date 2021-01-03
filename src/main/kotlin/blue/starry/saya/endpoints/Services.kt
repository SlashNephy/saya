package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.common.toBooleanFuzzy
import blue.starry.saya.common.toFFMpegPreset
import blue.starry.saya.services.CommentStreamManager
import blue.starry.saya.services.comments.withSession
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files

internal val m3u8 = ContentType("application", "x-mpegURL")

fun Route.getServiceHLSById() {
    get {
        val id: Int by call.parameters
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

fun Route.getServiceById() {
    get {
        val id: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Services.find { it.id == id }
        )
    }
}

fun Route.getServiceProgramsById() {
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

fun Route.getServiceM2TSById() {
    get {
        val id: Int by call.parameters
        val service = MirakurunDataManager.Services.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)
        val priority = call.parameters["priority"]?.toIntOrNull()

        MirakurunApi.getServiceStream(service.internalId, decode = true, priority = priority).receive { channel: ByteReadChannel ->
            call.respondBytesWriter {
                while (!channel.isClosedForRead) {
                    val byte = channel.readByte()
                    writeByte(byte)
                }
            }
        }
    }
}

fun Route.getServiceXspfById() {
    get {
        val id: Int by call.parameters
        val service = MirakurunDataManager.Services.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName, "watch.xspf"
        ).toString())

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

fun Route.getMirakurunServiceById() {
    get {
        val id: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Services.find { it.id == id }?.json
        )
    }
}

fun Route.getMirakurunServices() {
    get {
        call.respond(
            MirakurunDataManager.Services.toList().map { it.json }
        )
    }
}

fun Route.wsServiceCommentsById() {
    webSocket {
        val id: Int by call.parameters
        val stream = CommentStreamManager.findByServiceId(id)
            ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Service $id is not found."))

//        val hashtag = call.parameters["hashtag"]
//        val sample = call.parameters["sample"] == "1"
//        val twitter = if (sample) {
//            stream.getOrCreateTwitterProvider(null)
//        } else {
//            hashtag?.let { stream.getOrCreateTwitterProvider(it) }
//        }

        stream.getOrCreateNicoLiveProvider().withSession {
            stream.comments.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }

//            twitter.withSession {
//                (twitter.comments.openSubscription() + nicoLive.comments.openSubscription())
//                nicoLive.comments.openSubscription().consumeEach {
//                    send(Json.encodeToString(it))
//                }
//            }
        }
    }
}
