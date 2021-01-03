package blue.starry.saya

import blue.starry.saya.common.Env
import blue.starry.saya.endpoints.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
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

                    route("mirakurun") {
                        getMirakurunServiceById()
                    }

                    route("comments") {
                        wsServiceCommentsById()
                    }
                }

                route("mirakurun") {
                    getMirakurunServices()
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

                    route("xspf") {
                        getProgramXspfById()
                    }

                    route("mirakurun") {
                        getMirakurunProgramById()
                    }
                }

                route("mirakurun") {
                    getMirakurunPrograms()
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

                    route("xspf") {
                        getChannelsXspfById()
                    }

                    route("mirakurun") {
                        getMirakurunChannelsByGroup()
                    }
                }

                route("mirakurun") {
                    getMirakurunChannels()
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

                        route("mirakurun") {
                            getMirakurunTunerProcessByIndex()
                        }
                    }

                    route("mirakurun") {
                        getMirakurunTunerByIndex()
                    }
                }

                route("mirakurun") {
                    getMirakurunTuners()
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

            route("records") {
                getRecords()
                putRecords()

                route("{id}") {
                    getRecord()
                    deleteRecord()

                    route("file") {
                        getRecordFile()
                        deleteRecordFile()
                    }

                    route("hls") {
                        getRecordHLSById()
                    }

                    route("m2ts") {
                        getRecordM2TSById()
                    }

                    route("xspf") {
                        getRecordXspfById()
                    }

                    route("comments") {
                        wsRecordCommentsById()
                    }
                }
            }

            route("reserves") {
                getReserves()
                putReserves()

                route("{id}") {
                    getReserve()
                    deleteReserve()
                }
            }

            route("recording") {
                getRecordings()
                putRecordings()

                route("{id}") {
                    getRecording()
                    deleteRecording()
                }
            }

            route("rules") {
                getRules()
                putRules()

                route("{id}") {
                    getRule()
                    deleteRule()
                }
            }

            route("genres") {
                getGenres()

                route("{id}") {
                    getGenre()

                    route("programs") {
                        getGenrePrograms()
                    }
                }
            }

            route("status") {
                route("storage") {
                    getStorageStatus()
                }

                route("comments") {
                    getCommentStatus()

                    route("{id}") {
                        getCommentStatusById()
                    }
                }
            }
        }
    }
}
