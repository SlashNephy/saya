package blue.starry.saya.models

data class CommentControl(
    val action: Action,
    val seconds: Double
) {
    enum class Action {
        Sync
    }
}
