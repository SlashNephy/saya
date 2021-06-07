package blue.starry.saya.services.gochan

import java.time.ZonedDateTime

data class GochanRes(
    val name: String,
    val mail: String,
    val userId: String?,
    val time: ZonedDateTime,
    val text: String
)

data class GochanSubjectItem(
    val threadId: String,
    val title: String,
    val resCount: Int
)

data class GochanThreadAddress(
    val server: String,
    val board: String,
    val id: String
)

data class GochanKakologThreadList(
    val server: String,
    val board: String,
    val filename: String?,
    val startAt: Long,
    val endAt: Long
)

data class GochanKakologThread(
    val list: GochanKakologThreadList,
    val id: String,
    val title: String,
    val url: String,
    val lines: Int
)
