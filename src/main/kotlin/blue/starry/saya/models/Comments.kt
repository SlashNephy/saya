package blue.starry.saya.models

import kotlinx.serialization.Serializable

/**
 * DPlayer 互換のコメントモデル
 */
@Serializable
data class Comment(
    /**
     * コメントの配信元 (e.g. "lv2646436", "#twitter")
     */
    val source: String,

    /**
     * コメントの投稿時間 (エポック秒)
     */
    val time: Long,

    /**
     * コメントの投稿時間 (ミリ秒部分)
     */
    val timeMs: Int,

    /**
     * コメントの投稿者名 / ユーザ ID
     */
    val author: String,

    /**
     * コメントの本文
     */
    val text: String,

    /**
     * コメントの hex 表記の色 (e.g. "#123456")
     */
    val color: String,

    /**
     * コメントの位置
     */
    val type: Position,

    /**
     * コメントのサイズ
     */
    val size: Size
) {
    enum class Position {
        right, top, bottom
    }

    enum class Size {
        normal, small, medium, big
    }
}

@Serializable
data class JikkyoChannel(
    val type: Channel.Type,
    val jk: Int? = null,
    val name: String,
    val serviceIds: Set<Int>,
    val tags: Set<String> = emptySet(),
    val isOfficial: Boolean = false,
    val miyouId: String? = null,
    val communities: Set<String> = emptySet(),
    val hashtags: Set<String> = emptySet()
)

@Serializable
data class TimeshiftCommentControl(
    val action: Action,
    val seconds: Double = 0.0
) {
    enum class Action {
        Ready, Resume, Pause, Sync
    }
}
