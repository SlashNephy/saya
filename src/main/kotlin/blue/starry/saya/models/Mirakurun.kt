package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    /**
     * ID
     */
    val id: Long,

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
    val serviceIds: List<Long>
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
    val serviceId: Long,

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
     * 終了時刻 (エポック秒)
     */
    val endAt: Long,

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
     * 番組のジャンル
     */
    val genres: List<Genre>,

    val meta: Meta
) {
    @Serializable
    data class Meta(
        val videoContainer: String?,
        val videoResolution: String?,
        val audioSamplingRate: Int?
    )

    enum class Genre {
        News,
        Sports,
        Information,
        Drama,
        Music,
        Variety,
        Cinema,
        Anime,
        Documentary,
        Theater,
        Hobby,
        Welfare,
        Etc
    }
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
