package blue.starry.saya.models

import blue.starry.jsonkt.JsonObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MirakurunService(
    /**
     * Mirakurun オブジェクト (非公開)
     */
    @Transient internal val json: JsonObject? = null,

    /**
     * サービス ID
     */
    val id: Long,

    /**
     * 実際の DTV サービス ID (非公開)
     */
    @Transient internal val actualId: Int = 0,

    /**
     * DTV サービス名
     */
    val name: String,

    /**
     * ロゴ ID
     */
    val logoId: Int?,

    /**
     * リモコン キー ID
     */
    val keyId: Int?,

    /**
     * チャンネル
     */
    val channel: String,

    /**
     * タイプ
     */
    val type: Definitions.Channel.Type
)

@Serializable
data class MirakurunChannel(
    /**
     * Mirakurun オブジェクト (非公開)
     */
    @Transient internal val json: JsonObject? = null,

    /**
     * チャンネルタイプ
     */
    val type: Definitions.Channel.Type,

    /**
     * チャンネルグループ
     */
    val group: String,

    /**
     * チャンネル名
     */
    val name: String,

    /**
     * サービスのリスト
     */
    val services: List<MirakurunService>
)

@Serializable
data class MirakurunServiceLogo(
    /**
     * ロゴ ID
     */
    val id: Int,

    /**
     * サービス
     */
    val service: MirakurunService,

    /**
     * ロゴデータ (PNG, base64)
     */
    val data: String
)

@Serializable
data class MirakurunProgram(
    /**
     * Mirakurun オブジェクト (非公開)
     */
    @Transient internal val json: JsonObject? = null,

    /**
     * 番組 ID
     */
    val id: Long,

    /**
     * サービス
     */
    val service: MirakurunService,

    /**
     * 開始時刻 (エポック秒)
     */
    val startAt: Long,

    /**
     * 番組の長さ (秒)
     */
    val duration: Int,

    /**
     * 番組名
     */
    val name: String,

    /**
     * 番組の説明欄
     */
    val description: String,

    /**
     * 番組のフラグ (e.g. "新")
     */
    val flags: List<String>,

    /**
     * 番組のジャンル ID のリスト
     */
    val genres: List<Int>,

    /**
     * 話数情報
     */
    val episode: Episode,

    /**
     * コンテナのビデオフォーマット情報
     */
    val video: Video,

    /**
     * コンテナのオーディオフォーマット情報
     */
    val audio: Audio
) {
    @Serializable
    data class Episode(
        val number: Int?,
        val title: String?
    )

    @Serializable
    data class Video(
        val type: String?,
        val resolution: String?,
        val content: Int?,
        val component: Int?
    )

    @Serializable
    data class Audio(
        val samplingRate: Int?,
        val component: Int?
    )
}

@Serializable
data class MirakurunTuner(
    /**
     * Mirakurun オブジェクト (非公開)
     */
    @Transient internal val json: JsonObject? = null,

    val index: Int,
    val name: String,
    val types: List<Definitions.Channel.Type>,
    val command: String?,
    val pid: Int?,
    val users: List<User>,
    val isAvailable: Boolean,
    val isRemote: Boolean,
    val isFree: Boolean,
    val isUsing: Boolean,
    val isFault: Boolean
) {
    @Serializable
    data class User(
        val id: String,
        val priority: Int,
        val agent: String?
    )
}

@Serializable
data class MirakurunTunerProcess(
    /**
     * Mirakurun オブジェクト (非公開)
     */
    @Transient internal val json: JsonObject? = null,

    val pid: Int
)
