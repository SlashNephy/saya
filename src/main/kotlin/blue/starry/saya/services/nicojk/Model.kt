package blue.starry.saya.services.nicojk

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*
import blue.starry.jsonkt.string
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

data class CommentLog(override val json: JsonObject): JsonModel {
    val packets by modelList("packet") { Packet(it) }

    data class Packet(override val json: JsonObject): JsonModel {
        private val chat by jsonObject

        val thread by chat.byLambda { it.jsonPrimitive.long }
        val no by chat.byLambda { it.jsonPrimitive.long }
        val vpos by chat.byLambda { it.jsonPrimitive.long }
        val date by chat.byLambda { it.jsonPrimitive.long }
        val mail by chat.byString { "" }
        val userId by chat.byString("user_id")
        val premium by chat.byLambda(default = false) { it.string == "1" }
        val anonymity by chat.byLambda(default = false) { it.string == "1" }
        val dateUsec by chat.byLambda("date_usec") { it.jsonPrimitive.long }
        val content by chat.byString
    }
}
