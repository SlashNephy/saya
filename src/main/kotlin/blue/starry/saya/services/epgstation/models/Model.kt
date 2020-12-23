package blue.starry.saya.services.epgstation.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.boolean
import blue.starry.jsonkt.delegation.int
import blue.starry.jsonkt.delegation.string

data class Error(override val json: JsonObject): JsonModel {
    val code by int
    val message by string
    val errors by string
}

data class Channel(override val json: JsonObject): JsonModel {
    val id by int
    val serviceId by int
    val networkId by int
    val name by string
    val remoteControlKeyId by int
    val hasLogoData by boolean
    val channelType by string
    val channel by string
    val type by int
}
