package blue.starry.saya.endpoints

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.models.TimeshiftCommentControl
import blue.starry.saya.services.CommentChannelManager
import blue.starry.saya.services.SayaMiyouTVApi
import blue.starry.saya.services.SayaTwitterClient
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.miyoutv.toSayaComment
import blue.starry.saya.services.nicojk.NicoJkApi
import blue.starry.saya.services.nicolive.NicoLiveApi
import blue.starry.saya.services.nicolive.NicoLiveCommentProvider
import blue.starry.saya.services.nicolive.toSayaComment
import blue.starry.saya.services.twitter.TwitterHashTagProvider
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToLong

private val logger = KotlinLogging.createSayaLogger("saya.endpoints")

private suspend fun DefaultWebSocketSession.rejectWs(message: () -> String) {
    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message()))
}

private suspend fun findChannel(target: String): JikkyoChannel? {
    return if (target.startsWith("jk")) {
        // jk*** から探す

        val jk = target.removePrefix("jk").toIntOrNull() ?: return null
        CommentChannelManager.Channels.find { it.jk == jk }
    } else {
        // Mirakurun 互換 Service ID から探す

        val serviceId = target.toLongOrNull() ?: return null
        val mirakurun = MirakurunDataManager.Services.find { it.id == serviceId } ?: return null
        CommentChannelManager.Channels.find { it.serviceIds.contains(mirakurun.actualId) }
    }
}

private enum class CommentSource(private vararg val aliases: String) {
    Nicolive("nico", "nicolive"),
    Twitter("twitter"),
    GoChan("5ch", "2ch");

    companion object {
        fun from(sources: String?): List<CommentSource> {
            return if (sources == null) {
                values().toList()
            } else {
                val t = sources.orEmpty().split(",")

                values().filter {
                    it.aliases.any { alias -> t.contains(alias) }
                }
            }
        }
    }
}

// TODO: BroadcastChannel の共有
fun Route.wsLiveComments() {
    webSocket {
        val target: String by call.parameters
        val sources = CommentSource.from(call.parameters["sources"])

        val channel = findChannel(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }
        val comments = BroadcastChannel<Comment>(1)

        if (CommentSource.Nicolive in sources) {
            launch {
                val (lv, data) = channel.tags.plus(channel.name).flatMap { tag ->
                    NicoLiveApi.getLivePrograms(tag).data
                        .filter { data ->
                            // 公式番組優先
                            !channel.isOfficial || data.tags.any { it.text == "ニコニコ実況" }
                        }.map {
                            it.id to NicoLiveApi.getEmbeddedData("https://live2.nicovideo.jp/watch/${it.id}")
                        }
                }.firstOrNull() ?: return@launch

                val provider = NicoLiveCommentProvider(channel, comments, data.site.relive.webSocketUrl, lv)
                provider.start()
            }
        }

        if (CommentSource.Twitter in sources && channel.hashtags.isNotEmpty() && SayaTwitterClient != null) {
            launch {
                val twitter = TwitterHashTagProvider(channel, comments, channel.hashtags, SayaTwitterClient)
                twitter.start()
            }
        }

        comments.openSubscription().consumeEach {
            send(Json.encodeToString(it))
        }
    }
}

fun Route.wsTimeshiftComments() {
    webSocket {
        val target: String by call.parameters

        // エポック秒
        val startAt: Long by call.parameters
        val endAt: Long by call.parameters
        val duration = (endAt - startAt).toInt()

        val sources = CommentSource.from(call.parameters["sources"])

        val channel = findChannel(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }
        val timeMs = AtomicLong(startAt * 1000)
        var pause = true

        // unit 秒ずつ分割して取得
        val unit = 600

        // コメント配信ループ
        suspend fun Channel<Comment>.sendLoop() {
            consumeEach { comment ->
                val currentMs = comment.time * 1000 + comment.timeMs
                val waitMs = currentMs - timeMs.get()
                if (waitMs < -10000) {
                    logger.trace { "破棄 ($waitMs) : $comment" }
                    return@consumeEach
                }

                delay(waitMs)

                while (pause) {
                    delay(100)
                }
                send(Json.encodeToString(comment))
                logger.trace { "配信: $comment" }

                timeMs.getAndUpdate { prev ->
                    maxOf(prev, currentMs)
                }
            }
        }

        if (CommentSource.GoChan in sources && channel.miyouId != null && SayaMiyouTVApi != null) {
            launch {
                val queue = Channel<Comment>(Channel.UNLIMITED)

                launch {
                    repeat(duration / unit) { i ->
                        SayaMiyouTVApi.getComments(
                            channel.miyouId,
                            (startAt + unit * i) * 1000,
                            minOf(startAt + unit * (i + 1), endAt) * 1000
                        ).data.comments.map {
                            it.toSayaComment()
                        }.forEach {
                            queue.send(it)
                        }
                    }
                }

                queue.sendLoop()
            }
        }

        if (CommentSource.Nicolive in sources && channel.jk != null) {
            launch {
                val queue = Channel<Comment>(Channel.UNLIMITED)

                launch {
                    repeat(duration / unit) { i ->
                        NicoJkApi.getComments(
                            "jk${channel.jk}",
                            startAt + unit * i,
                            minOf(startAt + unit * (i + 1), endAt)
                        ).packets.map {
                            it.chat.toSayaComment("ニコニコ実況過去ログAPI [jk${channel.jk}]")
                        }.forEach {
                            queue.send(it)
                        }
                    }
                }

                queue.sendLoop()
            }
        }

        // WS コントロール処理ループ
        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect {
            val control = try {
                Json.decodeFromString<TimeshiftCommentControl>(it.readText())
            } catch (t: Throwable) {
                logger.error(t) { "WS コントロール命令のパースに失敗しました。" }
                return@collect
            }

            when (control.action) {
                /**
                 * クライアントの準備ができ, コメントの配信を開始する命令
                 *   {"action": "Ready"}
                 *
                 * コメントの配信を再開する命令
                 *   {"action": "Resume"}
                 */
                TimeshiftCommentControl.Action.Ready,
                TimeshiftCommentControl.Action.Resume -> {
                    pause = false
                }

                /**
                 * コメントの配信を一時停止する命令
                 *   {"action": "Pause"}
                 */
                //
                TimeshiftCommentControl.Action.Pause -> {
                    pause = true
                }

                /**
                 * コメントの位置を同期する命令
                 *   {"action": "Sync", "seconds": 10.0}
                 */
                TimeshiftCommentControl.Action.Sync -> {
                    pause = false

                    timeMs.set(((startAt + control.seconds) * 1000).roundToLong())
                }
            }

            logger.debug { "クライアントの命令: $control" }
        }
    }
}
