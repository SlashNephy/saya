package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class TwitterHashTagStatistics(
    override val source: String,
    override val comments: Int,
    override val commentsPerMinute: Int
): CommentStatistics
