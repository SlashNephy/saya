package blue.starry.saya.common

import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 文字列を ffmpeg のプリセット定義に変換する
 */
internal fun String?.toFFMpegPreset(): FFMpegWrapper.Preset? {
    return when (this?.toLowerCase()) {
        "high", FFMpegWrapper.Preset.High.name -> FFMpegWrapper.Preset.High
        "medium", FFMpegWrapper.Preset.Medium.name -> FFMpegWrapper.Preset.Medium
        "low", FFMpegWrapper.Preset.Low.name -> FFMpegWrapper.Preset.Low
        else -> null
    }
}

/**
 * 文字列をあいまいに [Boolean] に変換する
 *
 *     1   -> true
 *  "true" -> true
 *   null  -> false
 *   else  -> false
 */
internal fun String?.toBooleanFuzzy(): Boolean {
    return when {
        this == null -> false
        toIntOrNull() == 1 -> true
        else -> toLowerCase().toBoolean()
    }
}

internal fun MutableCollection<String>.addAllFuzzy(vararg elements: Any) {
    addAll(elements.map { it.toString() })
}

internal suspend fun ApplicationCall.respondOrNotFound(message: Any?) {
    respond(message ?: HttpStatusCode.NotFound)
}

internal suspend inline fun <reified T: Any> WebSocketSession.send(content: T) {
    send(Frame.Text(
        Json.encodeToString(content)
    ))
}
