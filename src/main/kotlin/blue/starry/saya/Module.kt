package blue.starry.saya

import blue.starry.saya.common.Env
import blue.starry.saya.endpoints.getCommentStatus
import blue.starry.saya.endpoints.getCommentStatusById
import blue.starry.saya.endpoints.wsRecordCommentsById
import blue.starry.saya.endpoints.wsServiceCommentsById
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import mu.KotlinLogging
import kotlin.time.seconds
import kotlin.time.toJavaDuration

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 15.seconds.toJavaDuration()
        timeout = 30.seconds.toJavaDuration()
    }
    install(XForwardedHeaderSupport)
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        anyHost()
    }
    install(CallLogging) {
        logger = KotlinLogging.logger("saya.server")
        format { call ->
            when (val status = call.response.status()) {
                HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
                null -> ""
                else -> "$status: ${call.request.httpMethod.value} ${call.request.uri}"
            }
        }
    }

    routing {
        route(Env.SAYA_BASE_URI) {
            route("comments") {
                getCommentStatus()

                route("{id}") {
                    route("live") {
                        wsServiceCommentsById()

                        route("status") {
                            getCommentStatusById()
                        }
                    }

                    route("timeshift") {
                        wsRecordCommentsById()

                        route("status") {
                            getCommentStatusById()
                        }
                    }
                }
            }
        }
    }
}
