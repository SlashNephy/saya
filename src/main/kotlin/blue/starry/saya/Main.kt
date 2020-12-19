package blue.starry.saya

import blue.starry.saya.server.module
import io.ktor.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import mu.KotlinLogging

val logger = KotlinLogging.logger("saya")

fun main() {
    embeddedServer(CIO,
        port = Env.SAYA_PORT ?: 1017,
        host = Env.SAYA_HOST ?: "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}
