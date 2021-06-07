package blue.starry.saya.services

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.saya.common.Env
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.services.gochan.GochanClient
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import mu.KLogger
import mu.KotlinLogging
import kotlin.time.Duration

const val SayaUserAgent = "saya/2.0 (+https://github.com/SlashNephy/saya)"
private val logger: KLogger
    get() = KotlinLogging.createSayaLogger("saya.client")

fun createSayaHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(WebSockets)
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            requestTimeout = Duration.minutes(1).inWholeMilliseconds
        }

        Logging {
            level = LogLevel.INFO
            logger = object : Logger {
                private val logger = KotlinLogging.createSayaLogger("saya.http")

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

fun createSayaTwitterClient(): ApiClient? {
    val (ck, cs) = Env.TWITTER_CK to Env.TWITTER_CS
    val (at, ats) = Env.TWITTER_AT to Env.TWITTER_ATS
    if (ck == null || cs == null || at == null || ats == null) {
        logger.info { "Twitter の資格情報が設定されていません。Twitter 連携機能は提供しません。" }
        return null
    }

    return PenicillinClient {
        account {
            application(ck, cs)
            token(at, ats)
        }
        // For streaming API stability
        httpClient(Apache) {
            install(HttpTimeout) {
                socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            }
        }
    }
}

fun createSaya5chClient(): GochanClient? {
    val (hmKey, appKey) = Env.GOCHAN_HM_KEY to Env.GOCHAN_APP_KEY
    val (authUA, ua) = Env.GOCHAN_AUTH_UA to Env.GOCHAN_UA
    val authX2chUA = Env.GOCHAN_AUTH_X_2CH_UA
    if (hmKey == null || appKey == null || authUA == null || ua == null || authX2chUA == null) {
        logger.info { "5ch API への接続情報が設定されていません。5ch 連携機能は提供しません。" }
        return null
    }

    return GochanClient(hmKey, appKey, authUA, authX2chUA, ua)
}
