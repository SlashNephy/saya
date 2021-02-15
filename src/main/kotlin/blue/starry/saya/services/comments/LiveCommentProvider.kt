package blue.starry.saya.services.comments

import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * コメントを配信する共通インターフェース
 */
interface LiveCommentProvider {
    /**
     * 実況チャンネル
     */
    val channel: Definitions.Channel

    /**
     * コメントキュー
     */
    val queue: BroadcastChannel<Comment>

    /**
     * 購読
     */
    val subscription: Subscription

    /**
     * コメントの取得を開始する
     */
    suspend fun start()

    class Subscription {
        private val collection = mutableSetOf<UUID>()
        private val collectionLock = Mutex()

        suspend fun create(id: UUID) = collectionLock.withLock {
            collection.add(id)
        }

        suspend fun remove(id: UUID) = collectionLock.withLock {
            collection.remove(id)
        }

        suspend fun isEmpty() = collectionLock.withLock {
            collection.isEmpty()
        }
    }
}
