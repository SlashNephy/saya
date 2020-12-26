package blue.starry.saya.services

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.saya.common.Env
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import jp.annict.client.AnnictClient
import mu.KotlinLogging

val httpClient by lazy {
    HttpClient {
        install(WebSockets)
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        Logging {
            level = LogLevel.INFO
            logger = object : Logger {
                private val logger = KotlinLogging.logger("saya.http")

                override fun log(message: String) {
                    logger.trace { message }
                }
            }
        }

        defaultRequest {
            userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
        }
    }
}

val twitter by lazy {
    PenicillinClient {
        account {
            application(Env.TWITTER_CK, Env.TWITTER_CS)
            token(Env.TWITTER_AT, Env.TWITTER_ATS)
        }
        httpClient(httpClient)
    }
}

val annictClient by lazy {
    AnnictClient(Env.ANNICT_TOKEN)
}
