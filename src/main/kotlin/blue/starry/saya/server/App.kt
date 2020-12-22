package blue.starry.saya.server

import blue.starry.saya.Env
import blue.starry.saya.server.endpoints.getCommentStats
import blue.starry.saya.server.endpoints.getCommentStream
import blue.starry.saya.server.endpoints.getIndex
import blue.starry.saya.server.endpoints.getServiceHLS
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.websocket.*
import mu.KotlinLogging

fun Application.module() {
    install(Locations)
    install(WebSockets)
    install(XForwardedHeaderSupport)
    install(CallLogging) {
        logger = KotlinLogging.logger("saya.server")
    }

    routing {
        route(Env.SAYA_BASE_URI) {
            getIndex()
            getCommentStream()
            getCommentStats()
            getServiceHLS()
        }
    }
}
