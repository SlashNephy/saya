package blue.starry.saya.services

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.saya.common.Env
import blue.starry.saya.services.miyoutv.MiyouTVApi
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import jp.annict.client.AnnictClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.time.minutes

const val SayaUserAgent = "saya/1.0 (+https://github.com/SlashNephy/saya)"

val SayaHttpClient by lazy {
    HttpClient(CIO) {
        install(WebSockets)
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            requestTimeout = 1.minutes.toLongMilliseconds()
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
            userAgent(SayaUserAgent)
        }
    }
}

val SayaTwitterClient by lazy {
    PenicillinClient {
        account {
            application(Env.TWITTER_CK, Env.TWITTER_CS)
            token(Env.TWITTER_AT, Env.TWITTER_ATS)
        }
        httpClient(SayaHttpClient)
    }
}

val SayaAnnictClient by lazy {
    AnnictClient(Env.ANNICT_TOKEN)
}

val SayaMiyouTVApi by lazy {
    val login = runBlocking {
        MiyouTVApi.login(Env.MORITAPO_EMAIL, Env.MORITAPO_PASSWORD)
    }

    MiyouTVApi(login.token)
}
