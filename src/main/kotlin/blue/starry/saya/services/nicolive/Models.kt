package blue.starry.saya.services.nicolive

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedData(
    val program: Program,
    val site: Site
) {
    @Serializable
    data class Program(
        val title: String,
        val nicoliveProgramId: String
    )

    @Serializable
    data class Site(
        val relive: Relive
    ) {
        @Serializable
        data class Relive(
            val webSocketUrl: String
        )
    }
}

@Serializable
data class SearchPrograms(
    val data: List<Data>
) {
    @Serializable
    data class Data(
        val id: String,
        val title: String,
        val tags: List<Tag>
    ) {
        @Serializable
        data class Tag(val text: String)
    }
}

@Serializable(with = NicoliveWebSocketSystemJsonDeserializer::class)
sealed class NicoliveWebSocketSystemJson {
    abstract val type: String

    @Serializable
    data class Default(
        override val type: String,
    ) : NicoliveWebSocketSystemJson()

    @Serializable
    data class Seat(
        override val type: String,
        val data: Data
    ) : NicoliveWebSocketSystemJson() {
        @Serializable
        data class Data(
            val keepIntervalSec: Long
        )
    }

    @Serializable
    data class Room(
        override val type: String,
        val data: Data
    ) : NicoliveWebSocketSystemJson() {
        @Serializable
        data class Data(
            val messageServer: MessageServer,
            val threadId: JsonElement
        ) {
            @Serializable
            data class MessageServer(val uri: String)
        }
    }

    @Serializable
    data class Statistics(
        override val type: String,
        val data: Data
    ) : NicoliveWebSocketSystemJson() {
        @Serializable
        data class Data(
            val comments: Int
        )
    }
}

@Serializable
data class NicoliveWebSocketMessageJson(
    val chat: Chat? = null
)

@Serializable
data class Chat(
    val thread: String,
    val no: Int,
    val vpos: Int,
    val date: Long,
    @SerialName("date_usec") val dateUsec: Int,
    val mail: String = "",
    @SerialName("user_id") val userId: String,
    val anonymity: Int = 0,
    val premium: Int = 0,
    val deleted: Int = 0,
    val content: String
)
