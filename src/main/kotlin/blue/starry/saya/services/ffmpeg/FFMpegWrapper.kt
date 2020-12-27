package blue.starry.saya.services.ffmpeg

import blue.starry.saya.common.addAllFuzzy
import blue.starry.saya.common.Env
import blue.starry.saya.models.Service
import blue.starry.saya.services.SayaUserAgent
import blue.starry.saya.services.mirakurun.MirakurunApi
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FFMpegWrapper {
    private val logger = KotlinLogging.logger("saya.FFMpegWrapper")
    val TmpDir: Path = Paths.get(Env.SAYA_TMP_DIR)

    /**
     * Mirakurun の M2TS ストリームを HLS に変換する
     */
    @ExperimentalStdlibApi
    fun startliveHLS(service: Service, preset: Preset, subTitle: Boolean): Pair<Process, Path> {
        val builder = ProcessBuilder()

        // stderr を受け取る
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        // work dir
        if (!Files.exists(TmpDir)) {
            Files.createDirectories(TmpDir)
        }
        builder.directory(TmpDir.toFile())

        // outout
        val output = TmpDir.resolve("live_${service.id}_${preset.name}.m3u8")

        buildList {
            add("ffmpeg")

            // ハードウェアアクセラレーション
            // "-hwaccel", "vaapi",

            // 入力
            addAllFuzzy(
                // "-re",
                "-dual_mono_mode", "main",
                "-user-agent", SayaUserAgent,
                "-i", "${MirakurunApi.ApiBaseUri}/services/${service.internalId}/stream?decode=1",
                // "-max_muxing_queue_size", "2048"
            )

            // HLS
            addAllFuzzy(
                "-f", "hls",
                "-hls_segment_type", "mpegts",
                "-hls_base_url", "${Env.SAYA_BASE_URI.removeSuffix("/")}/segments/",
                "-hls_time", Env.SAYA_HLS_SEGMENT_SEC,
                "-g", Env.SAYA_HLS_SEGMENT_SEC * 30,
                "-hls_list_size", Env.SAYA_HLS_SEGMENT_SIZE,
                "-hls_allow_cache", "0",
                "-hls_flags", "+delete_segments+omit_endlist",
                "-hls_delete_threshold", "8",
                "-hls_segment_filename", "live_${service.id}_${preset.name}_%09d.ts"
            )

            // 映像
            addAllFuzzy(
                "-c:v", "libx264",
                "-vb", preset.vb,
                "-vf", "yadif=0:-1:1",
                "-aspect", "${preset.width}:${preset.height}",
                "-preset", "ultrafast",
                "-r", "30000/1001"
            )

            // 音声
            addAllFuzzy(
                "-c:a", "libfdk_aac",
                "-ab", preset.ab,
                "-ar", preset.ar,
                "-ac", "2"
            )

            // 字幕
             if (subTitle) {
                addAllFuzzy("-map", "0", "-ignore_unknown")
             } else {
                 add("-sn")
             }

            // その他
            addAllFuzzy(
                "-threads", "0",
                "-flags", "+loop+global_header",
                "-movflags", "+faststart",
                "-hide_banner",
                "-loglevel", "fatal"
            )

            // 出力
            add(output.fileName.toString())
        }.also {
            builder.command(it)

            logger.trace { "Process: $it" }
        }

        return builder.start() to output
    }

    sealed class Preset(val name: String, val width: Int, val height: Int, val vb: String, val ab: String, val ar: Int) {
        object High: Preset("1080p", 1920, 1080, "5700k", "192k", 48000)
        object Medium: Preset("720p", 1280, 720, "4000k", "192k", 48000)
        object Low: Preset("360p", 640, 360, "1500k", "128k", 48000)
    }
}
