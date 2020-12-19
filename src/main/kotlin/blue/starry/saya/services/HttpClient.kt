package blue.starry.saya.services

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import mu.KotlinLogging

val httpClient = HttpClient {
    install(WebSockets)
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }

    Logging {
        level = LogLevel.INFO
        logger = object : Logger {
            private val logger = KotlinLogging.logger("saya.service")

            override fun log(message: String) {
                logger.trace { message }
            }
        }
    }

    defaultRequest {
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
    }
}
