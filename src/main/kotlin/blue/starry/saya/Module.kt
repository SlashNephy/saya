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
                        wsCommentStream()
                    }

                    route("stats") {
                        getCommentStats()
                    }
                }
            }

            route("services") {
                getServices()
                putServices()

                route("{id}") {
                    getServiceById()

                    route("hls") {
                        getServiceHLSById()
                    }

                    route("programs") {
                        getServiceProgramsById()
                    }

                    route("m2ts") {
                        getServiceM2TSById()
                    }

                    route("xspf") {
                        getServiceXspfById()
                    }
                }
            }

            route("segments") {
                route("{filename}") {
                    getSegmentByFilename()
                }
            }

            route("subscriptions") {
                getSubscriptions()

                route("hls") {
                    getHLSSubscriptions()
                }

                route("comments") {
                    getCommentSubscriptions()
                }

                route("events") {
                    getEventSubscriptions()
                }
            }

            route("events") {
                route("stream") {
                    wsEventStream()
                }
            }

            route("programs") {
                getPrograms()
                putPrograms()

                route("{id}") {
                    getProgramById()

                    route("m2ts") {
                        getProgramM2TSById()
                    }
                }
            }

            route("channels") {
                getChannels()
                putChannels()

                route("{group}") {
                    getChannelsByGroup()

                    route("m2ts") {
                        getChannelsM2TSByGroup()
                    }
                }
            }

            route("tuners") {
                getTuners()
                putTuners()

                route("{index}") {
                    getTunerByIndex()

                    route("process") {
                        getTunerProcessByIndex()
                        deleteTunerProcessByIndex()
                    }
                }
            }

            route("logos") {
                getLogos()
                putLogos()

                route("{id}") {
                    getLogoById()

                    route("png") {
                        getLogoPngById()
                    }
                }
            }
        }
    }
}
