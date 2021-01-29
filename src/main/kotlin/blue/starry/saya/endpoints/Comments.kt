package blue.starry.saya.endpoints

import blue.starry.saya.services.CommentStreamManager
import blue.starry.saya.services.comments.withSession
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private suspend fun DefaultWebSocketSession.reject(message: () -> String) {
    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message()))
}

fun Route.wsLiveComments() {
    webSocket {
        val serviceId: Long by call.parameters

        val service = MirakurunDataManager.Services.find {
            it.id == serviceId
        } ?: return@webSocket reject { "Service $serviceId is not found." }
        val stream = CommentStreamManager.Streams.find {
            it.channel.serviceIds.contains(service.actualId)
        } ?: return@webSocket reject { "Service $serviceId is not found." }

        stream.getOrCreateNicoLiveProvider().withSession {
            stream.getOrCreateTwitterProvider().withSession {
                stream.comments.openSubscription().consumeEach {
                    send(Json.encodeToString(it))
                }
            }
        }
    }
}

fun Route.wsTimeshiftComments() {
    webSocket {
//        val id: Long by call.parameters
//
//        val record = ChinachuDataManager.Recorded.find {
//            it.program.id == id
//        } ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Record $id is not found."))
//        val stream = CommentStreamManager.Streams.find {
//            it.channel.serviceIds.contains(record.program.service.actualId)
//        } ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Record $id is not found."))
//        val jkId = "jk${
//            stream.channel.jk ?: return@webSocket close(
//                CloseReason(
//                    CloseReason.Codes.CANNOT_ACCEPT,
//                    "Record $id is not found."
//                )
//            )
//        }"
//        val broadcaster = BroadcastChannel<Comment>(1)
//        var time = record.program.startAt.toDouble()
//
//        // unit 秒ずつ分割して取得
//        val unit = 600
//
//        launch {
//            val comments = BroadcastChannel<Comment>(1)
//
//            launch {
//                repeat(record.program.duration / unit) { i ->
//                    SayaMiyouTVApi.getComments(
//                        stream.channel.miyouId!!,
//                        (record.program.startAt + unit * i) * 1000,
//                        minOf(
//                            record.program.startAt + unit * (i + 1),
//                            record.program.startAt + record.program.duration
//                        ) * 1000
//                    ).data.comments.mapIndexed { index, it ->
//                        it.toSayaComment(index)
//                    }.forEach {
//                        comments.send(it)
//                    }
//                }
//            }
//
//            comments.openSubscription().consumeEach { comment ->
//                val waitMs = (comment.time - time).times(1000).roundToLong()
//                delay(waitMs)
//                broadcaster.send(comment)
//
//                time = comment.time
//            }
//        }
//
//        launch {
//            val comments = BroadcastChannel<Comment>(1)
//
//            launch {
//                repeat(record.program.duration / unit) { i ->
//                    NicoJkApi.getComments(
//                        jkId,
//                        record.program.startAt + unit * i,
//                        minOf(record.program.startAt + unit * (i + 1), record.program.startAt + record.program.duration)
//                    ).packets.map {
//                        it.chat.toSayaComment(record.program.service.name)
//                    }.forEach {
//                        comments.send(it)
//                    }
//                }
//            }
//
//            comments.openSubscription().consumeEach { comment ->
//                val waitMs = (comment.time - time).times(1000).roundToLong()
//                delay(waitMs)
//                broadcaster.send(comment)
//
//                time = comment.time
//            }
//        }
//
//        launch {
//            broadcaster.openSubscription().consumeEach {
//                send(Json.encodeToString(it))
//            }
//        }
//
//        incoming.consumeAsFlow().filterIsInstance<Frame.Text>().collect {
//            val control = Json.decodeFromString<CommentControl>(it.readText())
//
//            when (control.action) {
//                CommentControl.Action.Sync -> {
//                    time = record.program.startAt + control.seconds
//                }
//            }
//        }
    }
}
