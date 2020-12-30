package blue.starry.saya.models

import blue.starry.jsonkt.JsonElement
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: Type,
    val resource: Resource,
    val action: Action,
    val data: JsonElement
) {
    enum class Type {
        Data
    }

    enum class Resource {
        Service, Channel, Program, Tuner, Logo, Genre
    }

    enum class Action {
        Enumerate, Create, Update
    }
}
