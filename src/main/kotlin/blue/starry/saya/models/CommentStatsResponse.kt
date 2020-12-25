package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class CommentStatsResponse(
    val nico: NicoCommentStatistics?,
    val twitter: List<TwitterHashTagStatistics>,
)
