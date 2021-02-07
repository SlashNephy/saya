package blue.starry.saya.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.nio.file.Path

@Serializable
data class File(
    val id: String,
    @Transient internal val path: Path? = null,
    val filename: String,
    val size: Long
)

@Serializable
data class FileInfo(
    val file: File,
    val name: String,
    val description: String?,
    val extended: String?,
    val service: MirakurunService?,
    val startAt: Long,
    val endAt: Long,
    val duration: Int,
    val genres: List<Genre>
) {
    @Serializable
    data class Genre(
        val category: String,
        val name: String
    )
}
