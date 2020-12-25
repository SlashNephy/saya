package blue.starry.saya.services.comments

import blue.starry.saya.models.CommentStatistics

/**
 * コメントに関する統計情報を提供する共通インターフェイス
 */
interface CommentStatisticsProvider {
    /**
     * コメントに関する統計情報を提供する
     */
    fun provide(): CommentStatistics
}
