package blue.starry.saya.services.chinachu

import blue.starry.saya.models.RecordedProgram
import blue.starry.saya.models.RecordingProgram
import blue.starry.saya.models.ReservedProgram
import blue.starry.saya.models.User
import blue.starry.saya.services.mirakurun.programFlagRegex
import kotlinx.serialization.json.jsonPrimitive
import java.text.Normalizer
import blue.starry.saya.models.Program as SayaProgram
import blue.starry.saya.models.Rule as SayaRule

fun Program.toSayaProgram(): SayaProgram {
    return SayaProgram(
        id = id.toLong(36),
        serviceId = channel.sid,
        startAt = start / 1000,
        duration = seconds,
        name = fullTitle.replace(programFlagRegex, " ").let {
            Normalizer.normalize(it, Normalizer.Form.NFKC)
        },
        description = buildString {
            appendLine(description)
            appendLine()

            extra?.forEach {
                appendLine("◇ ${it.key}\n${it.value.jsonPrimitive.content}")
            }
        }.let {
              Normalizer.normalize(it, Normalizer.Form.NFKC)
        }.trim(),
        flags = flags,
        genres = listOf(category.toSayaGenre()),
        episode = SayaProgram.Episode(
            number = episode,
            title = subTitle.let {
                if (it.isBlank()) {
                    null
                } else {
                    it
                }
            }
        ),
        video = SayaProgram.Video(
            null, null, null, null
        ),
        audio = SayaProgram.Audio(
            null, null
        )
    )
}

fun Recorded.toSayaRecordedProgram(): RecordedProgram {
    return RecordedProgram(
        program = toSayaProgram(),
        path = recorded,
        priority = priority,
        tuner = null,
        rule = null,
        user = User(0, "Chinachu"),
        isManual = false,
        isConflict = isConflict
    )
}

fun Reserve.toSayaReservedProgram(): ReservedProgram {
    return ReservedProgram(
        program = toSayaProgram()
    )
}

fun Recording.toSayaRecordingProgram(): RecordingProgram {
    return RecordingProgram(
        program = toSayaProgram()
    )
}

fun Rule.toSayaRule(index: Int): SayaRule {
    return SayaRule(
        id = index.toLong(),
        isEnabled = !isDisabled,
        name = "#$index",
        description = "Chinachu からインポートされました。",
        user = User(0, "Chinachu")
    )
}

private val chinachuGenres = mapOf(
    "news" to 0x0,
    "sports" to 0x1,
    "information" to 0x2,
    "drama" to 0x3,
    "music" to 0x4,
    "variety" to 0x5,
    "cinema" to 0x6,
    "anime" to 0x7,
    "documentary" to 0x8,
    "theater" to 0x9,
    "hobby" to 0xA,
    "welfare" to 0xB
)
private fun String.toSayaGenre(): Int {
    val lv1 = chinachuGenres[this] ?: 0xF
    val lv2 = 0xF

    return lv1 * 16 + lv2
}
