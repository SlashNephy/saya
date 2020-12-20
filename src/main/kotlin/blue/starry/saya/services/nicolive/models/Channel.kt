package blue.starry.saya.services.nicolive.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class Channel(override val json: JsonObject): JsonModel {
    val name by string
    val serviceIds by intList
    val tags by stringList
    val official by boolean { false }
}
