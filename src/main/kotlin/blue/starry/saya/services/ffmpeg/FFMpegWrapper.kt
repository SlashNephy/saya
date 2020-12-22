package blue.starry.saya.services.ffmpeg

import blue.starry.saya.Env
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.models.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FFMpegWrapper {
    val TmpDir: Path = Paths.get("tmp")

    /**
     * Mirakurun の M2TS ストリームを HLS に変換する
     */
    fun startliveHLS(service: Service, preset: Preset, segmentSec: Int, segmentSize: Int): Pair<Process, Path> {
        val builder = ProcessBuilder()

        // stdout, stderr を受け取る
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        // work dir
        if (!Files.exists(TmpDir)) {
            Files.createDirectories(TmpDir)
        }
        builder.directory(TmpDir.toFile())

        // outout
        val output = TmpDir.resolve("live_${service.serviceId}_${preset.name}.m3u8")

        builder.command(
            "ffmpeg",
            // ハードウェアアクセラレーション
            // "-hwaccel", "vaapi",
            // 入力
            "-dual_mono_mode", "main",
            "-i", "${MirakurunApi.ApiBaseUri}/services/${service.id}/stream?decode=1",
            "-user-agent", "saya/1.0 (+https://github.com/SlashNephy/saya)",
            // HLS
            "-f", "hls",
            "-hls_segment_type", "mpegts",
            "-hls_base_url", "${Env.SAYA_BASE_URI.removeSuffix("/")}/segments/",
            "-hls_time", "$segmentSec",
            "-g", "${segmentSec * 30}",
            "-hls_list_size", "$segmentSize",
            "-hls_allow_cache", "0",
            "-hls_flags", "delete_segments",
            "-hls_segment_filename", "live_${service.serviceId}_${preset.name}_%06d.ts",
            // 映像
            "-c:v", "libx264",
            "-vb", preset.vb,
            "-vf", "yadif=0:-1:1",
            "-aspect", "${preset.width}:${preset.height}",
            "-preset", "superfast",
            "-r", "30000/1001",
            // 音声
            "-c:a", "libfdk_aac",
            "-ab", preset.ab,
            "-ar", "${preset.ar}",
            "-ac", "2",
            // 字幕
            // 字幕無効化: "-sn",
            "-map", "0", "-ignore_unknown",
            // その他
            "-flags", "+loop+global_header",
            "-movflags", "+faststart",
            "-hide_banner",
            "-loglevel", "error",
            // 出力
            output.fileName.toString()
        )

        return builder.start() to output
    }

    sealed class Preset(val name: String, val width: Int, val height: Int, val vb: String, val ab: String, val ar: Int) {
        object High: Preset("1080p", 1920, 1080, "6800k", "192k", 48000)
        object Medium: Preset("720p", 1280, 720, "4800k", "192k", 48000)
        object Low: Preset("360p", 640, 360, "1500k", "128k", 48000)
    }
}
