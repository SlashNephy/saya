package blue.starry.saya.services.mirakurun

import blue.starry.saya.common.Env
import blue.starry.saya.models.RecordedProgram
import blue.starry.saya.models.Service
import blue.starry.saya.services.ffmpeg.FFMpegWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.time.seconds
import kotlin.time.toKotlinDuration

object MirakurunStreamManager {
    private val logger = KotlinLogging.logger("saya.mirakurun")
    private val mutex = Mutex()
    private val streams = mutableListOf<Session>()

    suspend fun openLiveHLS(service: Service, preset: FFMpegWrapper.Preset, subTitle: Boolean): Path {
        mutex.withLock {
            // 終了したストリームを掃除
            streams.removeIf { it.job.isCompleted || !it.process.isAlive }

            val previous = streams.find { it.id == service.id && it.preset == preset }
            if (previous != null) {
                previous.mark()
                return previous.path
            }

            if (streams.size >= Env.SAYA_MAX_FFMPEG_PROCESSES) {
                error("Too many ffmpeg processes are running. So we cannot create new one.")
            }

            val (process, path) = FFMpegWrapper.startLiveHLS(service, preset, subTitle)

            streams += Session(service.id, preset, path, process)
            return path
        }
    }

    suspend fun openRecordHLS(record: RecordedProgram, preset: FFMpegWrapper.Preset, subTitle: Boolean): Path {
        mutex.withLock {
            // 終了したストリームを掃除
            streams.removeIf { it.job.isCompleted || !it.process.isAlive }

            val previous = streams.find { it.id == record.program.id && it.preset == preset }
            if (previous != null) {
                previous.mark()
                return previous.path
            }

            if (streams.size >= Env.SAYA_MAX_FFMPEG_PROCESSES) {
                error("Too many ffmpeg processes are running. So we cannot create new one.")
            }

            val (process, path) = FFMpegWrapper.startRecordHLS(record, preset, subTitle)

            streams += Session(record.program.id, preset, path, process)
            return path
        }
    }

    private data class Session(val id: Long, val preset: FFMpegWrapper.Preset, val path: Path, val process: Process) {
        private val mutex = Mutex()
        private var lastAccess = Instant.now()

        /**
         * アクセスを記録する
         */
        suspend fun mark() {
            mutex.withLock {
                lastAccess = Instant.now()
            }
        }

        val job = GlobalScope.launch {
            val limit = 15.seconds

            while (isActive) {
                delay(limit)

                mutex.withLock {
                    val duration = Duration.between(lastAccess, Instant.now()).toKotlinDuration()

                    // 既定の時間以上アクセスがなかったら自動でストリームを停止
                    if (limit < duration) {
                        process.destroy()
                        logger.trace { "Killed $process (ID: $id, Preset: ${preset.name})" }

                        cancel()
                    }
                }
            }
        }
    }
}
