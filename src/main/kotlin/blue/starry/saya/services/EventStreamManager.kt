package blue.starry.saya.services

import blue.starry.jsonkt.*
import blue.starry.saya.models.Event
import blue.starry.saya.services.mirakurun.*
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.seconds
import blue.starry.saya.services.mirakurun.Event as MirakurunEvent

object EventStreamManager {
    private val logger = KotlinLogging.logger("saya.events")
    val Stream = BroadcastChannel<Event>(1)

    suspend fun enumerate(): List<Event> {
        return listOf(
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Service,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Services.toList()
            ),
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Channel,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Channels.toList()
            ),
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Program,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Programs.toList()
            ),
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Tuner,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Tuners.toList()
            ),
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Logo,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Logos.toList()
            ),
            Event(
                type = Event.Type.Data,
                resource = Event.Resource.Genre,
                action = Event.Action.Enumerate,
                data = MirakurunDataManager.Genres.toList()
            )
        )
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
                    val event = json.parseObject { MirakurunEvent(it) }
                    handleEvent(event)
                }
                is JsonArray -> {
                    val events = json.parseArray { MirakurunEvent(it) }
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

    private suspend fun handleEvent(event: MirakurunEvent) {
        when (event.resource) {
            MirakurunEvent.Resource.Program -> {
                val program = Program(event.data).toSayaProgram()

                when (event.type) {
                    MirakurunEvent.Type.Create -> MirakurunDataManager.Programs.add(program)
                    MirakurunEvent.Type.Update,
                    MirakurunEvent.Type.Redefine -> MirakurunDataManager.Programs.replace(program) { it.id == program.id }
                }

                Stream.send(Event(
                    type = Event.Type.Data,
                    resource = Event.Resource.Program,
                    action = if (event.type == MirakurunEvent.Type.Create) Event.Action.Create else Event.Action.Update,
                    data = program
                ))
            }
            MirakurunEvent.Resource.Service -> {
                val service = Service(event.data).toSayaService()

                when (event.type) {
                    MirakurunEvent.Type.Create -> MirakurunDataManager.Services.add(service)
                    MirakurunEvent.Type.Update,
                    MirakurunEvent.Type.Redefine -> MirakurunDataManager.Services.replace(service) { it.id == service.id }
                }

                Stream.send(Event(
                    type = Event.Type.Data,
                    resource = Event.Resource.Service,
                    action = if (event.type == MirakurunEvent.Type.Create) Event.Action.Create else Event.Action.Update,
                    data = service
                ))
            }
            MirakurunEvent.Resource.Tuner -> {
                val tuner = Tuner(event.data).toSayaTuner()

                when (event.type) {
                    MirakurunEvent.Type.Create -> MirakurunDataManager.Tuners.add(tuner)
                    MirakurunEvent.Type.Update,
                    MirakurunEvent.Type.Redefine -> MirakurunDataManager.Tuners.replace(tuner) { it.index == tuner.index }
                }

                Stream.send(Event(
                    type = Event.Type.Data,
                    resource = Event.Resource.Tuner,
                    action = if (event.type == MirakurunEvent.Type.Create) Event.Action.Create else Event.Action.Update,
                    data = tuner
                ))
            }
        }

        logger.trace { event }
    }
}
