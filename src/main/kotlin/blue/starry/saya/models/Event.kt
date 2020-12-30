package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: Type,
    val resource: Resource,
    val action: Action,
    val data: String
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
