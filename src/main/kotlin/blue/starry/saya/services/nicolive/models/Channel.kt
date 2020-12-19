package blue.starry.saya.services.nicolive.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.boolean
import blue.starry.jsonkt.delegation.string
import blue.starry.jsonkt.delegation.stringList

data class Channel(override val json: JsonObject): JsonModel {
    val name by string
    val tags by stringList
    val official by boolean { false }
}
