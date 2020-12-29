package blue.starry.saya.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Service(
    /**
     * Mirakurun API 用 ID (非公開)
     */
    @Transient internal val internalId: Long = 0,

    /**
     * DTV サービス ID
     */
    val id: Int,

    /**
     * DTV サービス名
     */
    val name: String,

    /**
     * ロゴ ID
     */
    val logoId: Int?,

    /**
     * チャンネル
     */
    val channel: String
)

@Serializable
data class Channel(
    /**
     * チャンネルタイプ
     */
    val type: Type,

    /**
     * チャンネルグループ
     */
    val group: String,

    /**
     * チャンネル名
     */
    val name: String,

    /**
     * DTV サービス ID のリスト
     */
    val serviceIds: List<Int>
) {
    enum class Type {
        GR, BS, CS, SKY
    }
}

@Serializable
data class Logo(
    /**
     * ロゴ ID
     */
    val id: Int,

    /**
     * DTV サービス ID
     */
    val serviceId: Int,

    /**
     * ロゴデータ (PNG, base64)
     */
    val data: String
)

@Serializable
data class Program(
    /**
     * 番組 ID
     */
    val id: Long,

    /**
     * DTV サービス ID
     */
    val serviceId: Int,

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

    val meta: Meta
) {
    @Serializable
    data class Meta(
        val videoContainer: String?,
        val videoResolution: String?,
        val audioSamplingRate: Int?
    )
}

@Serializable
data class Tuner(
    val index: Int,
    val name: String,
    val types: List<Channel.Type>,
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
data class TunerProcess(
    val pid: Int
)
