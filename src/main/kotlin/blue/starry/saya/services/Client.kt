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

const val SayaUserAgent = "saya/2.0 (+https://github.com/SlashNephy/saya)"

val SayaHttpClient = run {
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

val SayaTwitterClient = run {
    val (ck, cs) = Env.TWITTER_CK to Env.TWITTER_CS
    val (at, ats) = Env.TWITTER_AT to Env.TWITTER_ATS
    if (ck == null || cs == null || at == null || ats == null) {
        return@run null
    }

    PenicillinClient {
        account {
            application(ck, cs)
            token(at, ats)
        }
        httpClient(SayaHttpClient)
    }
}

val SayaAnnictClient = run {
    AnnictClient(Env.ANNICT_TOKEN ?: return@run null)
}

val SayaMiyouTVApi = run {
    val (email, pass) = Env.MORITAPO_EMAIL to Env.MORITAPO_PASSWORD
    if (email == null || pass == null) {
        return@run null
    }

    val login = runBlocking {
        MiyouTVApi.login(email, pass)
    }

    MiyouTVApi(login.token)
}
