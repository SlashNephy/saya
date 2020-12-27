package blue.starry.saya.services.mirakurun

import blue.starry.jsonkt.*
import blue.starry.saya.models.Logo
import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import kotlin.time.hours
import kotlin.time.seconds

object MirakurunDataManager {
    private val logger = KotlinLogging.logger("saya.MirakurunDataManager")

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

    init {
        GlobalScope.launch {
            while (isActive) {
                delay(10.seconds)

                try {
                    MirakurunApi.getEventStream().receive { channel: ByteReadChannel ->
                        readEventStream(channel)
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "Error in connectEventStream" }
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
            Event.Resource.program -> {
                val program = Program(event.data).toSayaProgram()

                when (event.type) {
                    Event.Type.create -> Programs.add(program)
                    Event.Type.update,
                    Event.Type.redefine -> Programs.replace(program) { it.id == program.id }
                }
            }
            Event.Resource.service -> {
                val service = Service(event.data).toSayaService()

                when (event.type) {
                    Event.Type.create -> Services.add(service)
                    Event.Type.update,
                    Event.Type.redefine -> Services.replace(service) { it.id == service.id }
                }
            }
            Event.Resource.tuner -> {
                val tuner = Tuner(event.data).toSayaTuner()

                when (event.type) {
                    Event.Type.create -> Tuners.add(tuner)
                    Event.Type.update,
                    Event.Type.redefine -> Tuners.replace(tuner) { it.index == tuner.index }
                }
            }
        }

        logger.trace { event }
    }

    class ReadOnlyContainer<T: Any>(private val block: suspend () -> List<T>) {
        private val mutex = Mutex()
        private val collection = mutableListOf<T>()

        init {
            GlobalScope.launch {
                while (isActive) {
                    update()
                    delay(1.hours)
                }
            }
        }

        suspend fun update() {
            mutex.withLock {
                val new = block()

                collection.clear()
                collection.addAll(new)
            }
        }

        suspend fun add(new: T) {
            mutex.withLock {
                collection.add(new)
            }
        }

        suspend fun replace(new: T, predicate: (T) -> Boolean) {
            mutex.withLock {
                val index = collection.indexOfFirst(predicate)

                if (index < 0) {
                    add(new)
                } else {
                    collection[index] = new
                }
            }
        }

        suspend fun find(predicate: (T) -> Boolean): T? {
            return mutex.withLock {
                collection.find(predicate)
            }
        }

        suspend fun filter(predicate: (T) -> Boolean): List<T> {
            return mutex.withLock {
                collection.filter(predicate)
            }
        }

        suspend fun toList(): List<T> {
            return mutex.withLock {
                collection.toList()
            }
        }
    }
}
