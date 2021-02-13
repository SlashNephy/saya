package blue.starry.saya.services.mirakc

import blue.starry.saya.common.Env
import blue.starry.saya.common.ReadOnlyContainer
import blue.starry.saya.models.File
import org.apache.commons.codec.digest.DigestUtils
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.streams.toList
import java.nio.file.Files as JavaFiles

object MountPointManager {
    val Files = ReadOnlyContainer<File> {
        val path = Env.MOUNT_POINT?.let { Paths.get(it) } ?: return@ReadOnlyContainer null

        JavaFiles.walk(path).filter { it.extension == "m2ts" }.map {
            File(
                id = DigestUtils.sha1Hex(it.toString()),
                path = it,
                filename = it.fileName.toString(),
                size = JavaFiles.size(it)
            )
        }.toList()
    }
}
