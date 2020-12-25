package blue.starry.saya.models

/**
 * コメントに関する統計情報
 */
interface CommentStatistics {
    /**
     * コメントの配信元 (e.g. "lv2646436", "#twitter")
     */
    val source: String

    /**
     * コメント数
     */
    val comments: Int

    /**
     * コメント流速 (分)
     */
    val commentsPerMinute: Int
}
