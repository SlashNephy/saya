package blue.starry.saya.endpoints

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.rejectWs
import blue.starry.saya.common.respondOr404
import blue.starry.saya.models.Comment
import blue.starry.saya.models.CommentSource
import blue.starry.saya.models.TimeshiftCommentControl
import blue.starry.saya.services.CommentChannelManager
import blue.starry.saya.services.SayaMiyouTVApi
import blue.starry.saya.services.miyoutv.toSayaComment
import blue.starry.saya.services.nicojk.NicoJkApi
import blue.starry.saya.services.nicolive.toSayaComment
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
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

fun Route.wsLiveCommentsByTarget() {
    webSocket {
        val target: String by call.parameters
        val sources = CommentSource.from(call.parameters["sources"])

        val channel = CommentChannelManager.findByTarget(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }

        CommentChannelManager.subscribeLiveComments(channel, sources).consumeEach {
            send(Json.encodeToString(it))
        }
    }
}

fun Route.wsTimeshiftCommentsByTarget() {
    webSocket {
        val target: String by call.parameters

        // エポック秒
        val startAt: Long by call.parameters
        val endAt: Long by call.parameters
        val duration = (endAt - startAt).toInt()

        val sources = CommentSource.from(call.parameters["sources"])

        val channel = CommentChannelManager.findByTarget(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }
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

        if (CommentSource.Gochan in sources && channel.miyoutvId != null) {
            launch {
                val client = SayaMiyouTVApi ?: return@launch
                val queue = Channel<Comment>(Channel.UNLIMITED)

                launch {
                    repeat(duration / unit) { i ->
                        client.getComments(
                            channel.miyoutvId,
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

        if (CommentSource.Nicolive in sources && channel.nicojkId != null) {
            launch {
                val queue = Channel<Comment>(Channel.UNLIMITED)

                launch {
                    repeat(duration / unit) { i ->
                        NicoJkApi.getComments(
                            "jk${channel.nicojkId}",
                            startAt + unit * i,
                            minOf(startAt + unit * (i + 1), endAt)
                        ).packets.map {
                            it.chat.toSayaComment(
                                source = "ニコニコ実況過去ログAPI [jk${channel.nicojkId}]",
                                sourceUrl = "https://jikkyo.tsukumijima.net/api/kakolog/jk${channel.nicojkId}?starttime=${it.chat.date}&endtime=${it.chat.date + 1}&format=json"
                            )
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

fun Route.getCommentInfo() {
    get {
        call.respond(
            NicoJkApi.getChannels().toList()
        )
    }
}

fun Route.getCommentInfoByTarget() {
    get {
        val target: String by call.parameters

        call.respondOr404 {
            val channel = CommentChannelManager.findByTarget(target) ?: return@respondOr404 null

            NicoJkApi.getChannels().find { it.channel.nicojkId == channel.nicojkId }
        }
    }
}
