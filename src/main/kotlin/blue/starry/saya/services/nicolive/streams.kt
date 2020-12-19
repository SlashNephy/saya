package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.parseObject
import blue.starry.jsonkt.toJsonObject
import blue.starry.saya.services.nicolive.models.Channel
import java.nio.file.Paths
import kotlin.io.path.readText

val streams = Paths.get("channels.json").readText().toJsonObject().map { entry ->
    val channel = entry.value.parseObject { Channel(it) }
    Stream(entry.key, channel)
}
