package blue.starry.saya.services.gochan

import java.time.LocalDateTime

data class GochanRes(
    val number: Int,
    val name: String,
    val mail: String,
    val userId: String?,
    val date: LocalDateTime?,
    val text: String
)

data class GochanThread(
    val url: String?,
    val id: String,
    val title: String?,
    val resCount: Int,
    val reses: MutableList<GochanRes>
)

data class GochanBoard(
    val url: String,
    val title: String,
    val threads: MutableList<GochanThread>
)
