package blue.starry.saya.endpoints

import blue.starry.saya.common.Env
import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.common.toBooleanFuzzy
import blue.starry.saya.common.toFFMpegPreset
import blue.starry.saya.models.Comment
import blue.starry.saya.models.CommentControl
import blue.starry.saya.models.RecordedFile
import blue.starry.saya.services.CommentStreamManager.Streams
import blue.starry.saya.services.SayaMiyouTVApi
import blue.starry.saya.services.chinachu.ChinachuApi
import blue.starry.saya.services.chinachu.ChinachuDataManager
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import blue.starry.saya.services.mirakurun.MirakurunStreamManager
import blue.starry.saya.services.miyoutv.toSayaComment
import blue.starry.saya.services.nicojk.NicoJkApi
import blue.starry.saya.services.nicolive.toSayaComment
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToLong

fun Route.getRecords() {
    get {
        call.respond(
            ChinachuDataManager.Recorded.toList()
        )
    }
}

fun Route.putRecords() {
    get {
        ChinachuDataManager.Recorded.update()
        call.respond(HttpStatusCode.OK)
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

fun Route.wsRecordCommentsById() {
    webSocket {
        val id: Long by call.parameters

        val record = ChinachuDataManager.Recorded.find { it.program.id == id }
            ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Record $id is not found."))
        val stream = Streams.find {
            it.channel.serviceIds.contains(record.program.serviceId)
        }
            ?: return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Record $id is not found."))
        val jkId = "jk${
            stream.channel.jk ?: return@webSocket this.close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    "Record $id is not found."
                )
            )
        }"
        val broadcaster = BroadcastChannel<Comment>(1)
        var time = record.program.startAt.toDouble()

        // unit 秒ずつ分割して取得
        val unit = 600

        launch {
            val comments = BroadcastChannel<Comment>(1)

            launch {
                repeat(record.program.duration / unit) { i ->
                    SayaMiyouTVApi.getComments(
                        stream.channel.miyouId!!,
                        (record.program.startAt + unit * i) * 1000,
                        minOf(
                            record.program.startAt + unit * (i + 1),
                            record.program.startAt + record.program.duration
                        ) * 1000
                    ).data.comments.mapIndexed { index, it ->
                        it.toSayaComment(index)
                    }.forEach {
                        comments.send(it)
                    }
                }
            }

            comments.openSubscription().consumeEach { comment ->
                val waitMs = (comment.time - time).times(1000).roundToLong()
                delay(waitMs)
                broadcaster.send(comment)

                time = comment.time
            }
        }

        launch {
            val comments = BroadcastChannel<Comment>(1)

            launch {
                repeat(record.program.duration / unit) { i ->
                    NicoJkApi.getComments(
                        jkId,
                        record.program.startAt + unit * i,
                        minOf(record.program.startAt + unit * (i + 1), record.program.startAt + record.program.duration)
                    ).packets.mapIndexed { index, it ->
                        it.chat.toSayaComment(record.program.serviceId.toString())
                    }.forEach {
                        comments.send(it)
                    }
                }
            }

            comments.openSubscription().consumeEach { comment ->
                val waitMs = (comment.time - time).times(1000).roundToLong()
                delay(waitMs)
                broadcaster.send(comment)

                time = comment.time
            }
        }

        launch {
            broadcaster.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }
        }

        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect {
            val control = Json.decodeFromString<CommentControl>(it.readText())

            when (control.action) {
                CommentControl.Action.Sync -> {
                    time = record.program.startAt + control.seconds
                }
            }
        }
    }
}

fun Route.getRecordM2TSById() {
    get {
        val id: Long by call.parameters
        val record = ChinachuDataManager.Recorded.find { it.program.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondFile(File(record.path))
    }
}

fun Route.getRecordXspfById() {
    get {
        val id: Long by call.parameters
        val record = ChinachuDataManager.Recorded.find { it.program.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName, "watch.xspf"
        ).toString())

        call.respondTextWriter(ContentType("application", "xspf+xml")) {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">")
            appendLine("<trackList>")
            appendLine("<track>")
            val url = (Env.SAYA_URL_PREFIX ?: "http://${Env.SAYA_HOST}:${Env.SAYA_PORT}") + "${Env.SAYA_BASE_URI.removeSuffix("/")}/records/${record.program.id}/m2ts"
            appendLine("<location>$url</location>")
            appendLine("<title>${record.program.name}</title>")
            appendLine("</track>")
            appendLine("</trackList>")
            appendLine("</playlist>")
        }
    }
}

fun Route.getRecordHLSById() {
    get {
        val id: Long by call.parameters
        val record = ChinachuDataManager.Recorded.find { it.program.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        val preset = call.parameters["preset"].toFFMpegPreset() ?: FFMpegWrapper.Preset.High
        val subTitle = call.parameters["subtitle"].toBooleanFuzzy()

        val playlist = MirakurunStreamManager.openRecordHLS(record, preset, subTitle)

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
