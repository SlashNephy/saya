package blue.starry.saya

import blue.starry.saya.server.module
import io.ktor.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO,
        port = Env.SAYA_PORT,
        host = Env.SAYA_HOST,
        module = Application::module
    ).start(wait = true)
}
