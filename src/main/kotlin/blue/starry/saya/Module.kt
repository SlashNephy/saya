package blue.starry.saya

import blue.starry.saya.common.Env
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.endpoints.getIndex
import blue.starry.saya.endpoints.wsLiveComments
import blue.starry.saya.endpoints.wsTimeshiftComments
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
        method(HttpMethod.Get)
        header(HttpHeaders.Origin)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.Accept)
        header(HttpHeaders.ContentType)
        maxAgeInSeconds = 3600
        allowCredentials = true
    }
    install(CallLogging) {
        logger = KotlinLogging.createSayaLogger("saya.server")
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
            getIndex()

            route("comments") {
                route("{target}") {
                    route("live") {
                        wsLiveComments()
                    }

                    route("timeshift") {
                        wsTimeshiftComments()
                    }
                }
            }
        }
    }
}
