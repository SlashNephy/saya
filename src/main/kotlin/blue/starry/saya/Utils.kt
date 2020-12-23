package blue.starry.saya

import blue.starry.saya.services.ffmpeg.FFMpegWrapper

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
