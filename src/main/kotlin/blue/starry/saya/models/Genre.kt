package blue.starry.saya.models

import kotlinx.serialization.Serializable


@Serializable
data class Genre(
    val id: Int,
    val main: String,
    val sub: String,
    val count: Int
)
