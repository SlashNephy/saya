package blue.starry.saya.services.nicolive.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class NicoLiveWebSocketSystemJson(override val json: JsonObject): JsonModel {
    val type by string
    val data by model { Data(it) }

    data class Data(override val json: JsonObject): JsonModel {
        // seat
        val keepIntervalSec by long

        // room
        val messageServer by model { MessageServer(it) }
        val threadId by jsonElement

        // statistics
        val viewers by int
        val comments by int
        val adPoints by int
        val giftPoints by int

        data class MessageServer(override val json: JsonObject): JsonModel {
            val uri by string
        }
    }
}

data class NicoLiveWebSocketMessageJson(override val json: JsonObject) : JsonModel {
    val chat by nullableModel { Chat(it) }

    data class Chat(override val json: JsonObject) : JsonModel {
        val thread by string
        val no by int
        val vpos by int
        val date by int
        val dateUsec by int("date_usec")
        val mail by nullableString
        val userId by string("user_id")
        val anonymity by int { 0 }
        val premium by int { 0 }
        val content by string
    }
}
