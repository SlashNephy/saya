package blue.starry.saya.endpoints

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.common.rejectWs
import blue.starry.saya.common.respondOr404
import blue.starry.saya.models.CommentSource
import blue.starry.saya.models.TimeshiftCommentControl
import blue.starry.saya.services.comments.CommentChannelManager
import blue.starry.saya.services.nicojk.NicoJkApi
import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.createSayaLogger("saya.endpoints")
private val jsonWithDefault = Json {
    encodeDefaults = true
}

fun Route.wsLiveCommentsByTarget() {
    webSocket {
        val target: String by call.parameters
        val sources = CommentSource.from(call.parameters["sources"])

        val channel = CommentChannelManager.findByTarget(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }

        CommentChannelManager.subscribeLiveComments(channel, sources).consumeEach {
            send(jsonWithDefault.encodeToString(it))
        }
    }
}

fun Route.wsTimeshiftCommentsByTarget() {
    webSocket {
        val target: String by call.parameters
        val startAt: Long by call.parameters
        val endAt: Long by call.parameters
        val sources = CommentSource.from(call.parameters["sources"])

        val channel = CommentChannelManager.findByTarget(target) ?: return@webSocket rejectWs { "Parameter target is invalid." }
        val controls = incoming.consumeAsFlow().filterIsInstance<Frame.Text>().mapNotNull {
            try {
                Json.decodeFromString<TimeshiftCommentControl>(it.readText())
            } catch (t: Throwable) {
                logger.error(t) { "TimeshiftCommentControl のパースに失敗しました: $it" }
                null
            }
        }

        CommentChannelManager.subscribeTimeshiftComments(channel, sources, controls, startAt, endAt).consumeEach {
            send(jsonWithDefault.encodeToString(it))
        }
    }
}

fun Route.getCommentInfo() {
    get {
        call.respond(
            try {
                NicoJkApi.getChannels().toList()
            } catch (e: ResponseException) {
                emptyList()
            }
        )
    }
}

fun Route.getCommentInfoByTarget() {
    get {
        val target: String by call.parameters

        call.respondOr404 {
            val channel = CommentChannelManager.findByTarget(target) ?: return@respondOr404 null

            try {
                NicoJkApi.getChannels().find { it.channel.nicojkId == channel.nicojkId }
            } catch (e: ResponseException) {
                null
            }
        }
    }
}
