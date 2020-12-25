package blue.starry.saya

import blue.starry.saya.common.Env
import blue.starry.saya.endpoints.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.seconds
import kotlin.time.toJavaDuration

fun Application.module() {
    install(Routing)
    install(Locations)
    install(Compression)
    install(WebSockets) {
        pingPeriod = 15.seconds.toJavaDuration()
        timeout = 30.seconds.toJavaDuration()
    }
    install(XForwardedHeaderSupport)
    install(ContentNegotiation) {
        json()
    }
    install(Sessions) {

    }
    install(CORS) {
        anyHost()
    }
    install(CallId) {
        retrieve {
            it.request.header("cf-request-id")
        }

        val counter = AtomicInteger()
        generate {
            DigestUtils.sha1Hex("${counter.getAndIncrement()}")
        }

        verify {
            it.isNotEmpty()
        }

        retrieveFromHeader("cf-request-id")
        replyToHeader("X-Request-Id")
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
                        wsCommentsStream()
                    }

                    route("stats") {
                        getCommentsStats()
                    }
                }
            }

            route("services") {
                getServices()

                route("{id}") {
                    getService()

                    route("hls") {
                        getServicesHLS()
                    }

                    route("programs") {
                        getServicePrograms()
                    }
                }
            }

            route("segments") {
                route("{filename}") {
                    getSegment()
                }
            }

            route("subscriptions") {
                getSubscriptions()

                route("hls") {
                    getSubscriptionsHLS()
                }

                route("comments") {
                    getSubscriptionsComments()
                }

                route("events") {
                    getSubscriptionsEvents()
                }
            }

            route("events") {
                route("stream") {
                    getEventsStream()
                }
            }

            route("programs") {
                getPrograms()

                route("{id}") {
                    getProgram()
                }
            }

            route("channels") {
                getChannels()

                route("{type}") {
                    getChannelsByType()

                    route("{group}") {
                        getChannelsByTypeAndGroup()

                        route("services") {
                            getChannelServicesByTypeAndGroup()
                        }
                    }
                }
            }

            route("tuners") {
                getTuners()

                route("{index}") {
                    getTuner()

                    route("process") {
                        getTunerProcess()
                        deleteTunerProcess()
                    }
                }
            }
        }
    }
}
