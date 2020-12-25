package blue.starry.saya.services.comments

import blue.starry.saya.models.Comment
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import java.util.concurrent.atomic.AtomicInteger

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
     * 現在の購読数
     */
    val subscriptions: AtomicInteger

    /**
     * コメントに関する統計情報
     */
    val stats: CommentStatisticsProvider

    /**
     * コメントを取得しているバックグラウンドタスク
     */
    val job: Job
}

/**
 * ストリームの購読数をチェックし自動で [CommentProvider] を閉じる
 */
suspend fun CommentProvider?.withSession(block: suspend () -> Unit) {
    try {
        this?.subscriptions?.getAndIncrement()

        block()
    } finally {
        if (this != null && subscriptions.decrementAndGet() <= 0) {
            subscriptions.set(0)
            job.cancel()
        }
    }
}
