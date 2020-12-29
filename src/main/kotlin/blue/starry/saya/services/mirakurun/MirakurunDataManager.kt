package blue.starry.saya.services.mirakurun

import blue.starry.jsonkt.*
import blue.starry.saya.common.ReadOnlyContainer
import blue.starry.saya.models.Genre
import blue.starry.saya.models.Logo
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import kotlin.time.seconds

object MirakurunDataManager {
    private val logger = KotlinLogging.logger("saya.mirakurun")

    val Services = ReadOnlyContainer {
        MirakurunApi.getServices().map { mirakurun ->
            mirakurun.toSayaService()
        }
    }

    val Channels = ReadOnlyContainer {
        MirakurunApi.getChannels().mapNotNull { mirakurun ->
            mirakurun.toSayaChannel()
        }
    }

    val Programs = ReadOnlyContainer {
        MirakurunApi.getPrograms().map { mirakurun ->
            mirakurun.toSayaProgram()
        }
    }

    val Tuners = ReadOnlyContainer {
        MirakurunApi.getTuners().map { mirakurun ->
            mirakurun.toSayaTuner()
        }
    }

    val Logos = ReadOnlyContainer {
        Services.filter {
            it.logoId != null
        }.distinctBy {
            it.logoId
        }.map { service ->
            Logo(
                id = service.logoId!!,
                serviceId = service.id,
                data = Base64.encodeBase64String(MirakurunApi.getServiceLogo(service.internalId))
            )
        }.sortedBy {
            it.id
        }
    }

    val Genres = ReadOnlyContainer {
        mainGenres.flatMap { (lv1, main) ->
            subGenres[lv1]!!.filterValues { it != null }.map { (lv2, sub) ->
                val id = lv1 * 16 + lv2

                Genre(
                    id = id,
                    main = main,
                    sub = sub!!,
                    count = Programs.filter { program ->
                        program.genres.contains(id)
                    }.count()
                )
            }
        }
    }

    init {
        GlobalScope.launch {
            while (isActive) {
                delay(10.seconds)

                try {
                    MirakurunApi.getEventStream().receive { channel: ByteReadChannel ->
                        readEventStream(channel)
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "Error in /event/stream" }
                }
            }
        }
    }

    private suspend fun readEventStream(channel: ByteReadChannel) {
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: continue
            val json = line.toJsonElementOrNull() ?: continue

            when (json) {
                is JsonObject -> {
                    val event = json.parseObject { Event(it) }
                    handleEvent(event)
                }
                is JsonArray -> {
                    val events = json.parseArray { Event(it) }
                    events.forEach { event ->
                        handleEvent(event)
                    }
                }
                else -> {
                    logger.warn { "Unknown JSON: $json" }
                }
            }
        }
    }

    private suspend fun handleEvent(event: Event) {
        when (event.resource) {
            Event.Resource.Program -> {
                val program = Program(event.data).toSayaProgram()

                when (event.type) {
                    Event.Type.Create -> Programs.add(program)
                    Event.Type.Update,
                    Event.Type.Redefine -> Programs.replace(program) { it.id == program.id }
                }
            }
            Event.Resource.Service -> {
                val service = Service(event.data).toSayaService()

                when (event.type) {
                    Event.Type.Create -> Services.add(service)
                    Event.Type.Update,
                    Event.Type.Redefine -> Services.replace(service) { it.id == service.id }
                }
            }
            Event.Resource.Tuner -> {
                val tuner = Tuner(event.data).toSayaTuner()

                when (event.type) {
                    Event.Type.Create -> Tuners.add(tuner)
                    Event.Type.Update,
                    Event.Type.Redefine -> Tuners.replace(tuner) { it.index == tuner.index }
                }
            }
        }

        logger.trace { event }
    }
}
