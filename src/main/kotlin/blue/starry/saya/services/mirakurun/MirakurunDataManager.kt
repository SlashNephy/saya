package blue.starry.saya.services.mirakurun

import blue.starry.saya.models.Channel
import blue.starry.saya.models.Program
import blue.starry.saya.models.Service
import blue.starry.saya.models.Tuner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.jsonPrimitive

object MirakurunDataManager {
    val Channels = ReadOnlyContainer {
        MirakurunApi.getChannels().mapNotNull { mirakurun ->
            Channel(
                Channel.Type.values().firstOrNull { it.name == mirakurun.type } ?: return@mapNotNull null,
                mirakurun.channel,
                mirakurun.name.orEmpty(),
                mirakurun.services.map { it.serviceId }
            )
        }
    }

    val Services = ReadOnlyContainer {
        MirakurunApi.getServices().mapNotNull { mirakurun ->
            Service(
                mirakurun.id,
                mirakurun.serviceId,
                mirakurun.name,
                if (mirakurun.hasLogoData) mirakurun.logoId else null,
                Channels.find {
                    it.group == mirakurun.channel.channel
                } ?: return@mapNotNull null
            )
        }
    }

    val Programs = ReadOnlyContainer {
        val flagRegex = "[【\\[(](新|終|再|字|デ|解|無|二|S|SS|初|生|Ｎ|映|多|双)[】\\])]".toRegex()

        MirakurunApi.getPrograms().map { mirakurun ->
            Program(
                mirakurun.id,
                mirakurun.serviceId,
                mirakurun.startAt / 1000,
                (mirakurun.startAt + mirakurun.duration) /10000,
                mirakurun.duration / 1000,
                mirakurun.name,
                buildString {
                    appendLine(mirakurun.description)
                    appendLine()

                    mirakurun.extended?.forEach {
                        append("\n◇${it.key}\n${it.value.jsonPrimitive.content}")
                    }
                }.trim(),
                mirakurun.name.replace("[無料]", "[無]").let {
                    flagRegex.findAll(it).map { match ->
                        match.groupValues[1]
                   }.toList()
                },
                mirakurun.genres.map {
                    Program.Genre.values().elementAtOrElse(it.lv1) {
                        Program.Genre.Etc
                    }
                }.distinct(),
                Program.Meta(
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
                mirakurun.index,
                mirakurun.name,
                mirakurun.types.mapNotNull { type ->
                    Channel.Type.values().firstOrNull { it.name == type }
                },
                mirakurun.command,
                mirakurun.pid,
                mirakurun.users.map {
                    Tuner.User(
                        it.id,
                        it.priority,
                        it.agent
                    )
                },
                mirakurun.isAvailable,
                mirakurun.isRemote,
                mirakurun.isFree,
                mirakurun.isUsing,
                mirakurun.isFault
            )
        }
    }

    class ReadOnlyContainer<T: Any>(private val block: suspend () -> List<T>) {
        private val mutex = Mutex()
        private val collection = mutableListOf<T>()

        init {
            GlobalScope.launch {
                update()
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
