package blue.starry.saya.services.chinachu

import blue.starry.saya.models.RecordedProgram
import blue.starry.saya.models.RecordingProgram
import blue.starry.saya.models.ReservedProgram
import blue.starry.saya.models.User
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.normalize
import blue.starry.saya.services.mirakurun.programFlagRegex
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import blue.starry.saya.models.Program as SayaProgram
import blue.starry.saya.models.Rule as SayaRule

private val logger = KotlinLogging.logger("saya.chinachu")

suspend fun Program.toSayaProgram(): SayaProgram? {
    val service = MirakurunDataManager.Services.find { it.actualId == channel.sid }
    if (service == null) {
        logger.warn { "Service is not found. ($this)" }
        return null
    }

    return SayaProgram(
        id = id.toLong(36),
        service = service,
        startAt = start / 1000,
        duration = seconds,
        name = fullTitle.normalize().replace(programFlagRegex, " ").trim(),
        description = buildString {
            appendLine(description)
            appendLine()

            extra?.forEach {
                appendLine("◇ ${it.key}\n${it.value.jsonPrimitive.content}")
            }
        }.normalize().replace(programFlagRegex, " ").trim(),
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

suspend fun Recorded.toSayaRecordedProgram(): RecordedProgram? {
    return RecordedProgram(
        program = toSayaProgram() ?: return null,
        path = recorded,
        priority = priority,
        tuner = null,
        rule = null,
        user = User(0, "Chinachu"),
        isManual = false,
        isConflict = isConflict
    )
}

suspend fun Reserve.toSayaReservedProgram(): ReservedProgram? {
    return ReservedProgram(
        program = toSayaProgram() ?: return null
    )
}

suspend fun Recording.toSayaRecordingProgram(): RecordingProgram? {
    return RecordingProgram(
        program = toSayaProgram() ?: return null
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
