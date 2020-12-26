package blue.starry.saya.services.mirakurun

import blue.starry.saya.models.*
import blue.starry.saya.models.Program
import blue.starry.saya.models.Tuner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.jsonPrimitive
import org.apache.commons.codec.binary.Base64
import java.util.*
import kotlin.time.hours

object MirakurunDataManager {
    val Channels = ReadOnlyContainer {
        MirakurunApi.getChannels().mapNotNull { mirakurun ->
            Channel(
                type = Channel.Type.values().firstOrNull { it.name == mirakurun.type } ?: return@mapNotNull null,
                group = mirakurun.channel,
                name = mirakurun.name.orEmpty(),
                services = mirakurun.services.map { it.serviceId }
            )
        }
    }

    val Services = ReadOnlyContainer {
        MirakurunApi.getServices().mapNotNull { mirakurun ->
            Service(
                id = mirakurun.id,
                serviceId = mirakurun.serviceId,
                name = mirakurun.name,
                logoId = if (mirakurun.hasLogoData) mirakurun.logoId else null,
                channel = Channels.find {
                    it.group == mirakurun.channel.channel
                } ?: return@mapNotNull null
            )
        }
    }

    val Programs = ReadOnlyContainer {
        val flagRegex = "[【\\[(](新|終|再|字|デ|解|無|二|S|SS|初|生|Ｎ|映|多|双)[】\\])]".toRegex()

        MirakurunApi.getPrograms().map { mirakurun ->
            Program(
                id = mirakurun.id,
                serviceId = mirakurun.serviceId,
                startAt = mirakurun.startAt / 1000,
                endAt = (mirakurun.startAt + mirakurun.duration) /10000,
                duration = mirakurun.duration / 1000,
                name = mirakurun.name,
                description = buildString {
                    appendLine(mirakurun.description)
                    appendLine()

                    mirakurun.extended?.forEach {
                        append("\n◇${it.key}\n${it.value.jsonPrimitive.content}")
                    }
                }.trim(),
                flags = mirakurun.name.replace("[無料]", "[無]").let {
                    flagRegex.findAll(it).map { match ->
                        match.groupValues[1]
                    }.toList()
                },
                genres = mirakurun.genres.map {
                    Program.Genre.values().elementAtOrElse(it.lv1) {
                        Program.Genre.Etc
                    }
                }.distinct(),
                meta = Program.Meta(
                    mirakurun.video?.type,
                    mirakurun.video?.resolution,
                    mirakurun.audio?.samplingRate
                )
            )
        }
    }

    val Tuners = ReadOnlyContainer {
        MirakurunApi.getTuners().map { mirakurun ->
            Tuner(
                index = mirakurun.index,
                name = mirakurun.name,
                types = mirakurun.types.mapNotNull { type ->
                    Channel.Type.values().firstOrNull { it.name == type }
                },
                command = mirakurun.command,
                pid = mirakurun.pid,
                users = mirakurun.users.map {
                    Tuner.User(
                        it.id,
                        it.priority,
                        it.agent
                    )
                },
                isAvailable = mirakurun.isAvailable,
                isRemote = mirakurun.isRemote,
                isFree = mirakurun.isFree,
                isUsing = mirakurun.isUsing,
                isFault = mirakurun.isFault
            )
        }
    }

    val Logos = ReadOnlyContainer {
        Services.filter {
            it.logoId != null
        }.distinctBy {
            it.logoId
        }.map { mirakurun ->
            Logo(
                id = mirakurun.logoId!!,
                serviceId = mirakurun.serviceId,
                data = Base64.encodeBase64String(MirakurunApi.getServiceLogo(mirakurun.id))
            )
        }.sortedBy {
            it.id
        }
    }

    class ReadOnlyContainer<T: Any>(private val block: suspend () -> List<T>) {
        private val mutex = Mutex()
        private val collection = mutableListOf<T>()

        init {
            GlobalScope.launch {
                while (true) {
                    update()
                    delay(1.hours)
                }
            }
        }

        suspend fun update() {
            mutex.withLock {
                collection.clear()
                collection.addAll(block())
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
