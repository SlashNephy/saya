package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class JikkyoChannel(
    val type: Channel.Type,
    val jk: Int? = null,
    val name: String,
    val serviceIds: Set<Int>,
    val tags: Set<String> = emptySet(),
    val isOfficial: Boolean = false,
    val miyouId: String? = null,
    val communities: Set<String> = emptySet()
)
