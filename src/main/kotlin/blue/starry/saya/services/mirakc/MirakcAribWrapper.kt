package blue.starry.saya.services.mirakc

import blue.starry.jsonkt.parseArray
import blue.starry.jsonkt.parseObject
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.File
import blue.starry.saya.models.FileInfo
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.mainGenres
import blue.starry.saya.common.normalize
import blue.starry.saya.services.mirakurun.subGenres
import mu.KotlinLogging
import java.nio.file.Path

class MirakcAribWrapper(private val executablePath: String) {
    private val logger = KotlinLogging.createSayaLogger("saya.mirakc")

    private fun execute(vararg arguments: String) = sequence {
        val pb = ProcessBuilder(executablePath, *arguments)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)

        val process = pb.start()
        logger.debug { "mirakc-arib spawned: ${pb.command()}" }

        process.inputStream.bufferedReader().useLines { seq ->
            yieldAll(seq)
        }

        if (process.waitFor() != 0) {
            throw RuntimeException("mirakc-arib の実行に失敗しました。")
        }
    }

    private fun syncClocks(path: Path) = execute("sync-clocks", path.toString()).first().parseArray {
        SyncClock(it)
    }

    private fun collectEits(serviceId: Int, path: Path) = execute("collect-eits", "--sids=$serviceId", path.toString()).map { line ->
        line.parseObject {
            CollectEit(it)
        }
    }

    suspend fun detectOnAir(file: File, halfWidth: Boolean): FileInfo? {
        val sync = syncClocks(file.path!!).first()
        val serviceId = sync.sid
        val time = sync.clock.time

        val events = collectEits(serviceId, file.path).filter {
            // https://github.com/youzaka/ariblib#%E4%BE%8B2-%E3%81%84%E3%81%BE%E6%94%BE%E9%80%81%E4%B8%AD%E3%81%AE%E7%95%AA%E7%B5%84%E3%81%A8%E6%AC%A1%E3%81%AE%E7%95%AA%E7%B5%84%E3%82%92%E8%A1%A8%E7%A4%BA
            it.sectionNumber == 0
        }.flatMap {
            it.events
        }.filter {
            time in it.startTime until (it.startTime + it.duration)
        }

        val firstEvent = events.first()
        var record = FileInfo(
            file = file,
            name = "",
            description = null,
            extended = null,
            service = MirakurunDataManager.Services.find { it.actualId == serviceId },
            startAt = firstEvent.startTime,
            endAt = firstEvent.startTime + firstEvent.duration,
            duration = firstEvent.duration,
            genres = emptyList()
        )

        events.flatMap { it.descriptors }.forEach { descriptor ->
            when (descriptor) {
                // 番組タイトル, 番組説明
                is CollectEit.Descriptor.ShortEvent -> {
                    record = record.copy(
                        name = descriptor.eventName ?: record.name,
                        description = descriptor.text ?: record.description
                    )
                }
                // 拡張番組説明
                is CollectEit.Descriptor.ExtendedEvent -> {
                    record = record.copy(
                        extended = descriptor.items.joinToString("\n") { "◇${it.first}\n${it.second}" }
                    )
                }
                // ジャンル
                is CollectEit.Descriptor.Content -> {
                    record = record.copy(
                        genres = descriptor.nibbles.map {
                            FileInfo.Genre(mainGenres[it[0]]!!, subGenres[it[0]]!![it[1]]!!)
                        }
                    )
                }
                // ビデオコーデック
                is CollectEit.Descriptor.Component -> {
                }
                // オーディオコーデック
                is CollectEit.Descriptor.AudioComponent -> {
                }
            }
        }

        return if (halfWidth) {
            record.copy(
                name = record.name.normalize(),
                description = record.description?.normalize(),
                extended = record.extended?.normalize()
            )
        } else {
            record
        }
    }
}
