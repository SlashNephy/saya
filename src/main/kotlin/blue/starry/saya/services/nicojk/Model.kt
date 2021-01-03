package blue.starry.saya.services.nicojk

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.model
import blue.starry.jsonkt.delegation.modelList
import blue.starry.saya.services.comments.nicolive.models.Chat

data class CommentLog(override val json: JsonObject): JsonModel {
    val packets by modelList("packet") { Packet(it) }

    data class Packet(override val json: JsonObject): JsonModel {
        val chat by model { Chat(it) }
    }
}
