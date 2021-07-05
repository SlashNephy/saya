package blue.starry.saya.models

import kotlinx.serialization.Serializable

/**
 * DPlayer 互換のコメントモデル
 */
@Serializable
data class Comment(
    /**
     * コメントの配信元
     */
    val source: String,

    /**
     * コメントの配信元の URL
     */
    val sourceUrl: String? = null,

    /**
     * コメントの投稿時間 (エポック秒)
     */
    val time: Long,

    /**
     * コメントの投稿時間 (ミリ秒部分)
     */
    val timeMs: Int,

    /**
     * タイムシフトコメントでの開始地点からの再生時間 (秒)
     */
    val seconds: Double? = null,

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
    val color: String = "#ffffff",

    /**
     * コメントの位置
     */
    val type: Position = Position.right,

    /**
     * コメントのサイズ
     */
    val size: Size = Size.normal
) {
    @Suppress("EnumEntryName")
    enum class Position {
        right, top, bottom
    }

    @Suppress("EnumEntryName")
    enum class Size {
        normal, small, medium, big
    }
}

@Serializable
data class TimeshiftCommentControl(
    val action: Action,
    val seconds: Double = 0.0
) {
    enum class Action {
        Ready, Resume, Pause, Sync
    }
}

@Serializable
data class CommentInfo(
    val channel: Definitions.Channel,
    val force: Int,
    val last: String
)

enum class CommentSource(private vararg val aliases: String) {
    Nicolive("nico", "nicolive"),
    Twitter("twitter"),
    Gochan("5ch", "2ch");

    companion object {
        fun from(sources: String?): List<CommentSource> {
            return if (sources == null) {
                values().toList()
            } else {
                val t = sources.split(",")

                values().filter {
                    it.aliases.any { alias -> alias in t }
                }
            }
        }
    }
}
