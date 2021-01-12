package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class CommentControl(
    val action: Action,
    val seconds: Double
) {
    enum class Action {
        Sync
    }
}
