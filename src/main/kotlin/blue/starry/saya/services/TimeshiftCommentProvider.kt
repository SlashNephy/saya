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
     * タイムシフトコメントの開始時刻 (エポック秒)
     */
    val startAt: Long

    /**
     * タイムシフトコメントの終了時刻 (エポック秒)
     */
    val endAt: Long

    /**
     * コメントの配信を開始する
     */
    suspend fun start()

    /**
     * コメントを取得する
     */
    suspend fun fetch()

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
