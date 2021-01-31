package blue.starry.saya.services

import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * コメントを配信する共通インターフェース
 */
interface CommentProvider {
    /**
     * 実況チャンネル
     */
    val channel: JikkyoChannel

    /**
     * コメントキュー
     */
    val comments: BroadcastChannel<Comment>
}
