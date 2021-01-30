package blue.starry.saya.endpoints

import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.models.TimeshiftCommentControl
import blue.starry.saya.services.CommentChannelManager
import blue.starry.saya.services.SayaMiyouTVApi
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
import kotlin.math.roundToLong

private val logger = KotlinLogging.logger("saya.endpoints")

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

        if (CommentSource.Twitter in sources) {
            launch {
                if (channel.hashtags.isEmpty()) {
                    return@launch
                }

                val twitter = TwitterHashTagProvider(channel, comments, channel.hashtags)
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
        val broadcaster = BroadcastChannel<Comment>(1)
        var time = startAt.toDouble()

        // unit 秒ずつ分割して取得
        val unit = 600

        if (CommentSource.GoChan in sources && channel.miyouId != null) {
            launch {
                val comments = BroadcastChannel<Comment>(1)

                launch {
                    repeat(duration / unit) { i ->
                        SayaMiyouTVApi.getComments(
                            channel.miyouId,
                            (startAt + unit * i) * 1000,
                            minOf(startAt + unit * (i + 1), endAt) * 1000
                        ).data.comments.map {
                            it.toSayaComment()
                        }.forEach {
                            comments.send(it)
                        }
                    }
                }

                comments.openSubscription().consumeEach { comment ->
                    val waitMs = (comment.time - time).times(1000).roundToLong()
                    delay(waitMs)
                    broadcaster.send(comment)

                    time = comment.time
                }
            }
        }

        if (CommentSource.Nicolive in sources && channel.jk != null) {
            launch {
                val comments = BroadcastChannel<Comment>(1)

                launch {
                    repeat(duration / unit) { i ->
                        NicoJkApi.getComments(
                            "jk${channel.jk}",
                            startAt + unit * i,
                            minOf(startAt + unit * (i + 1), endAt)
                        ).packets.map {
                            it.chat.toSayaComment("ニコニコ実況 過去ログ API")
                        }.forEach {
                            comments.send(it)
                        }
                    }
                }

                comments.openSubscription().consumeEach { comment ->
                    val waitMs = (comment.time - time).times(1000).roundToLong()
                    delay(waitMs)
                    broadcaster.send(comment)

                    time = comment.time
                }
            }
        }

        launch {
            broadcaster.openSubscription().consumeEach {
                send(Json.encodeToString(it))
            }
        }

        // WS コントロール処理ループ
        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect {
            val control = try {
                Json.decodeFromString<TimeshiftCommentControl>(it.readText())
            } catch (e: Throwable) {
                logger.error(e) { "WS コントロールの処理に失敗しました。" }
                return@collect
            }

            when (control.action) {
                /**
                 * クライアントの準備ができ, コメントの配信を開始する命令
                 *   {"action": "Ready"}
                 */
                TimeshiftCommentControl.Action.Ready -> {

                }

                /**
                 * コメントの配信を再開する命令
                 *   {"action": "Ready"}
                 */
                TimeshiftCommentControl.Action.Resume -> {

                }

                /**
                 * コメントの配信を一時停止する命令
                 *   {"action": "Pause"}
                 */
                //
                TimeshiftCommentControl.Action.Pause -> {

                }

                /**
                 * コメントの位置を同期する命令
                 *   {"action": "Ready", "seconds": 10.0}
                 */
                TimeshiftCommentControl.Action.Sync -> {
                    time = startAt + control.seconds
                }
            }

            logger.debug { "クライアントの命令: $control" }
        }
    }
}
