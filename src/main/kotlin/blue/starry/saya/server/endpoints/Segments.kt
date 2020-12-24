package blue.starry.saya.server.endpoints

import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.nio.file.Files
import java.nio.file.Paths

fun Route.getSegment() {
    get {
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
