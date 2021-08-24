package blue.starry.saya.services.nicolive

import kotlinx.serialization.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializer(forClass = NicoliveWebSocketSystemJson::class)
object NicoliveWebSocketSystemJsonDeserializer : KSerializer<NicoliveWebSocketSystemJson> {

    override fun deserialize(decoder: Decoder): NicoliveWebSocketSystemJson {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        require(element is JsonObject)
        val serializer = when (element["type"]?.jsonPrimitive?.content) {
            "seat" -> NicoliveWebSocketSystemJson.Seat.serializer()
            "room" -> NicoliveWebSocketSystemJson.Room.serializer()
            "statistics" -> NicoliveWebSocketSystemJson.Statistics.serializer()
            else -> NicoliveWebSocketSystemJson.Default.serializer()
        }
        return decoder.json.decodeFromJsonElement(serializer, element)
    }

    override fun serialize(encoder: Encoder, value: NicoliveWebSocketSystemJson) {
        throw NotImplementedError("Serialization of NicoliveWebSocketSystemJson is not impl")
    }
}