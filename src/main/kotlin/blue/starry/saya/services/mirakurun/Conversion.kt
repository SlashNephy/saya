package blue.starry.saya.services.mirakurun

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.saya.models.TunerProcess
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import java.text.Normalizer
import blue.starry.saya.models.Channel as SayaChannel
import blue.starry.saya.models.Program as SayaProgram
import blue.starry.saya.models.Service as SayaService
import blue.starry.saya.models.Tuner as SayaTuner

private val logger = KotlinLogging.logger("saya.")

fun Service.toSayaService(): SayaService {
    return SayaService(
        json = json,
        internalId = id,
        id = serviceId,
        name = name.normalize(),
        logoId = if (hasLogoData) logoId else null,
        keyId = remoteControlKeyId,
        channel = channel.channel
    )
}

internal fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFKC)
}

suspend fun Service.Channel.toSayaChannel(): SayaChannel? {
    return SayaChannel(
        json = json,
        type = SayaChannel.Type.values().firstOrNull { it.name == type } ?: return null,
        group = channel,
        name = name.orEmpty().normalize(),
        serviceIds = MirakurunDataManager.Services.filter {
            it.channel == channel
        }.map {
            it.id
        }
    )
}


fun Program.toSayaProgram(): SayaProgram? {
    val name = name?.normalize()?.replace(programFlagRegex, " ")?.trim() ?: return null
    val description = buildString {
        appendLine(description ?: return null)
        appendLine()

        val sections = extended?.map {
            it.key to it.value.jsonPrimitive.content
        }.orEmpty().run {
            if (video == null && audio == null) {
                return@run this
            }

            plus(
                "ストリーム情報" to buildString {
                    if (video != null) {
                        appendLine("【映像】${video!!.type.toUpperCase()} ${video!!.componentType.let { videoComponentTypes[it] }}")
                    }
                    if (audio != null) {
                        appendLine("【音声】${audio!!.samplingRate.let { audioSamplingRates[it] }} ${audio!!.componentType.let { audioComponentTypes[it] }}")
                    }
                }
            )
        }
        sections.forEach {
            appendLine("◇ ${it.first.removePrefix("◇")}\n${it.second}")
        }
    }.normalize().replace(programFlagRegex, " ").trim()

    return SayaProgram(
        json = json,
        id = id,
        serviceId = serviceId,
        startAt = startAt / 1000,
        duration = duration / 1000,
        name = name,
        description = description,
        flags = (this.name!!.toSayaFlags() + this.description!!.toSayaFlags()).distinct().toList(),
        genres = genres.map {
            it.toSayaGenre()
        }.distinct(),
        episode = SayaProgram.Episode(
            number = name.toSayaEpisodeNumber(),
            title = name.toSayaEpisodeTitle() ?: this.description!!.toSayaEpisodeTitle()
        ),
        video = SayaProgram.Video(
            type = video?.type,
            resolution = video?.resolution,
            content = video?.streamContent,
            component = video?.componentType
        ),
        audio = SayaProgram.Audio(
            samplingRate = audio?.samplingRate,
            component = audio?.componentType
        )
    )
}

internal val programFlagRegex = "[【\\[(](新|終|再|字|デ|解|無|二|S|SS|初|生|Ｎ|映|多|双)[】\\])]".toRegex()
private fun String.toSayaFlags(): Sequence<String> {
    return programFlagRegex.findAll(this).map {
        it.groupValues[1]
    }
}

private val programEpisodeNumberRegex = "(.)([\\d一壱二三四五六七八九十〇]+)(.)".toRegex()
internal fun String.toSayaEpisodeNumber(): Int? {
    val match = programEpisodeNumberRegex.findAll(this).find {
        val (prefix, _, suffix) = it.destructured

        when {
            prefix == "#" || prefix == "♯" -> true
            prefix == "第" && (suffix == "話" || suffix == "回") -> true
            prefix == "(" && suffix == ")" -> true
            else -> false
        }
    } ?: return null
    val text = match.groupValues[2]

    return text
        .replace('一', '1')
        .replace('壱', '1')
        .replace('二', '2')
        .replace('三', '3')
        .replace('四', '4')
        .replace('五', '5')
        .replace('六', '6')
        .replace('七', '7')
        .replace('八', '8')
        .replace('九', '9')
        .replace('十', '1')
        .replace('〇', '0')
        .removePrefix("0")
        .toIntOrNull()
        ?: run {
            logger.warn { "Failed to parse episode number: $text" }
            null
        }
}

private val programEpisodeTitleRegex = "[「【]([^」】]+)[」】]".toRegex()
internal fun String.toSayaEpisodeTitle(): String? {
    return programEpisodeTitleRegex.find(this)?.groupValues?.get(1)
}

fun Tuner.toSayaTuner(): SayaTuner {
    return SayaTuner(
        json = json,
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

fun Program.Genre.toSayaGenre(): Int {
    return lv1 * 16 + lv2
}

fun SayaTuner.toSayaTunerProcess(): TunerProcess? {
    return TunerProcess(
        json = jsonObjectOf(
            "pid" to pid
        ),
        pid = pid ?: return null
    )
}
