package blue.starry.saya.services.ffmpeg

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FFMpegWrapper {
    private val TmpDir: Path = Paths.get("tmp")

    fun hls(input: String, preset: Preset, subTitle: Boolean, segmentSec: Int, segmentSize: Int): Process {
        val builder = ProcessBuilder()

        val streamId = DigestUtils.sha256(input).let {
            Hex.encodeHexString(it, true).take(8)
        }
        val formatter = DateTimeFormatter
            .ofPattern("MMddHHmm")
        val date = formatter.format(LocalDateTime.now())

        // stdin を渡し stdout, stderr を受け取る
        // builder.redirectInput(ProcessBuilder.Redirect.PIPE)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        // work dir
        if (!Files.exists(TmpDir)) {
            Files.createDirectories(TmpDir)
        }
        builder.directory(TmpDir.toFile())

        builder.command(
            "ffmpeg",
            // 入力
            "-dual_mono_mode", "main",
            "-i", input,
            // HLS 固有設定
            "-f", "hls",
            "-hls_segment_type", "mpegts",
            "-hls_time", "$segmentSec",
            "-g", "${segmentSec * 30}",
            "-hls_list_size", "$segmentSize",
            "-hls_allow_cache", "0",
            "-hls_flags", "delete_segments",
            "-hls_segment_filename", "stream_${streamId}_${date}_%05d.m2ts",
            // 映像
            "-vcodec", "libx264",
            "-vb", preset.vb,
            "-vf", "yadif=0:-1:1,scale=${preset.width}:${preset.height}",
            "-aspect", "16:9",
            "-preset", "veryfast",
            "-r", "30000/1001",
            // 音声
            "-acodec", "aac",
            "-ab", preset.ab,
            "-ar", "${preset.ar}",
            "-ac", "2",
            // 字幕
            "-sn",
            // "-map", "0", "-ignore_unknown",
            // その他
            "-flags", "+loop+global_header",
            "-movflags", "+faststart",
            "-threads", "auto",
            "-hide_banner",
            "-loglevel", "error",
            // 出力
            "stream_$streamId.m3u8"
        )

        return builder.start()
    }

    sealed class Preset(val name: String, val width: Int, val height: Int, val vb: String, val ab: String, val ar: Int) {
        object High: Preset("1080p", 1920, 1080, "6800k", "192k", 48000)
        object Medium: Preset("720p", 1280, 720, "4800k", "192k", 48000)
        object Low: Preset("360p", 640, 360, "1500k", "128k", 48000)
    }
}
