package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class NicoCommentStatistics(
    override val source: String,
    override val comments: Int,
    override val commentsPerMinute: Int,
    val viewers: Int,
    val adPoints: Int,
    val giftPoints: Int
): CommentStatistics
