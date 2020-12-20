package blue.starry.saya.services.comments

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * コメントを配信する共通インターフェース
 */
interface CommentProvider {
    /**
     * [CommentProvider] を管理するストリーム
     */
    val stream: CommentStream

    /**
     * コメントを配信する [BroadcastChannel]
     */
    val comments: BroadcastChannel<Comment>

    /**
     * コメントに関する統計情報
     */
    val stats: CommentStatisticsProvider

    /**
     * コメントを取得しているバックグラウンドタスク
     */
    val job: Job
}
