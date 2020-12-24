package blue.starry.saya.server

import blue.starry.saya.Env
import blue.starry.saya.server.endpoints.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.websocket.*
import mu.KotlinLogging

fun Application.module() {
    install(WebSockets)
    install(XForwardedHeaderSupport)
    install(CORS) {
        anyHost()
    }
    install(CallLogging) {
        logger = KotlinLogging.logger("saya.server")
    }

    routing {
        route(Env.SAYA_BASE_URI) {
            getIndex()

            route("comments") {
                route("{target}") {
                    route("stream") {
                        wsCommentStream()
                    }

                    route("stats") {
                        getCommentStats()
                    }
                }
            }

            route("services") {
                route("{serviceId}") {
                    route("hls") {
                        getServiceHLS()
                    }
                }
            }

            route("segments") {
                route("{filename}") {
                    getSegment()
                }
            }
        }
    }
}
