package blue.starry.saya.services

import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import kotlinx.coroutines.channels.Channel

interface TimeshiftCommentProvider {
    /**
     * 実況チャンネル
     */
    val channel: Definitions.Channel

    /**
     * コメントキュー
     */
    val queue: Channel<Comment>

    /**
     * コメントの取得を開始する
     *
     * @param startAt 開始時刻 (エポック秒)
     * @param endAt 終了時刻 (エポック秒)
     */
    suspend fun start(startAt: Long, endAt: Long)

    /**
     * コメントの位置をシークする
     *
     * @param seconds コメントの秒数
     */
    suspend fun seek(seconds: Double)

    /**
     * コメントの配信を一時停止する
     */
    suspend fun pause()

    /**
     * コメントの配信を再開する
     */
    suspend fun resume()
}
