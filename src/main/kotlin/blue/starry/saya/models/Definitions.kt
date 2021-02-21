package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class Definitions(
    val channels: List<Channel>,
    val boards: List<Board>
) {
    @Serializable
    data class Channel(
        val name: String,
        val type: Type,
        val serviceIds: Set<Int>,
        val nicojkId: Int? = null,
        val hasOfficialNicolive: Boolean = false,
        val nicoliveTags: Set<String> = emptySet(),
        val nicoliveCommunityIds: Set<String> = emptySet(),
        val miyoutvId: String? = null,
        val twitterKeywords: Set<String> = emptySet(),
        val boardIds: Set<String> = emptySet(),
        val threadKeywords: Set<String> = emptySet(),
        val syobocalId: Int? = null,
        val annictId: Int? = null
    ) {
        enum class Type {
            GR, BS, CS, SKY
        }
    }

    @Serializable
    data class Board(
        val id: String,
        val name: String,
        val server: String,
        val board: String,
        val keywords: List<String> = emptyList()
    )
}
