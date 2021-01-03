package blue.starry.saya.services.comments

import kotlinx.coroutines.Job
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
 * アクティブな [CommentProvider] であるかどうか
 */
val CommentProvider?.isActive: Boolean
    get() = this?.job?.isActive == true

/**
 * ストリームの購読数をチェックし自動で [CommentProvider] を閉じる
 */
suspend fun <T: CommentProvider> T?.withSession(block: suspend (T?) -> Unit) {
    try {
        this?.subscriptions?.getAndIncrement()

        block(this)
    } finally {
        if (this != null && subscriptions.decrementAndGet() <= 0) {
            subscriptions.set(0)
            job.cancel()
        }
    }
}
