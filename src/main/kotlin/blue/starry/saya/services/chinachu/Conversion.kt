package blue.starry.saya.services.chinachu

import blue.starry.saya.models.RecordedProgram
import blue.starry.saya.models.RecordingProgram
import blue.starry.saya.models.ReservedProgram
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
        endAt = end / 1000,
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
        genres = listOf(SayaProgram.Genre.valueOf(category.capitalize())),
        meta = SayaProgram.Meta(
            null, null, null
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
        name = "#$index",
        description = "Chinachu からインポートされました。"
    )
}
