package blue.starry.saya.services.mirakurun

import kotlinx.serialization.json.jsonPrimitive
import java.text.Normalizer
import blue.starry.saya.models.Channel as SayaChannel
import blue.starry.saya.models.Program as SayaProgram
import blue.starry.saya.models.Service as SayaService
import blue.starry.saya.models.Tuner as SayaTuner

fun Service.toSayaService(): SayaService {
    return SayaService(
        internalId = id,
        id = serviceId,
        name = Normalizer.normalize(name, Normalizer.Form.NFKC),
        logoId = if (hasLogoData) logoId else null,
        channel = channel.channel
    )
}

suspend fun Service.Channel.toSayaChannel(): SayaChannel? {
    return SayaChannel(
        type = SayaChannel.Type.values().firstOrNull { it.name == type } ?: return null,
        group = channel,
        name = Normalizer.normalize(name.orEmpty(), Normalizer.Form.NFKC),
        serviceIds = MirakurunDataManager.Services.filter {
            it.channel == channel
        }.map {
            it.id
        }
    )
}

internal val programFlagRegex = "[【\\[(](新|終|再|字|デ|解|無|無料|二|S|SS|初|生|Ｎ|映|多|双)[】\\])]".toRegex()
fun Program.toSayaProgram(): SayaProgram {
    val name = Normalizer.normalize(name, Normalizer.Form.NFKC)

    return SayaProgram(
        id = id,
        serviceId = serviceId,
        startAt = startAt / 1000,
        endAt = (startAt + duration) / 1000,
        duration = duration / 1000,
        name = name.replace(programFlagRegex, " "),
        description = buildString {
            appendLine(description)
            appendLine()

            extended?.forEach {
                appendLine("◇ ${it.key}\n${it.value.jsonPrimitive.content}")
            }
        }.let {
            Normalizer.normalize(it, Normalizer.Form.NFKC)
        }.trim(),
        flags = programFlagRegex.findAll(name).map { match ->
            match.groupValues[1]
        }.toList(),
        genres = genres.map {
            SayaProgram.Genre.values().elementAtOrElse(it.lv1) {
                SayaProgram.Genre.Etc
            }
        }.distinct(),
        meta = SayaProgram.Meta(
            video?.type,
            video?.resolution,
            audio?.samplingRate
        )
    )
}

fun Tuner.toSayaTuner(): SayaTuner {
    return SayaTuner(
        index = index,
        name = name,
        types = types.mapNotNull { type ->
            SayaChannel.Type.values().firstOrNull { it.name == type }
        },
        command = command,
        pid = pid,
        users = users.map {
            SayaTuner.User(
                it.id,
                it.priority,
                it.agent
            )
        },
        isAvailable = isAvailable,
        isRemote = isRemote,
        isFree = isFree,
        isUsing = isUsing,
        isFault = isFault
    )
}
