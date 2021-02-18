package blue.starry.saya.services

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.saya.common.Env
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.services.annict.AnnictClient
import blue.starry.saya.services.gochan.GochanClient
import blue.starry.saya.services.mirakc.MirakcAribWrapper
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.miyoutv.MiyouTVApi
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
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.time.minutes

const val SayaUserAgent = "saya/2.0 (+https://github.com/SlashNephy/saya)"
private val logger = KotlinLogging.createSayaLogger("saya.client")

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

val SayaTwitterClient by lazy {
    val (ck, cs) = Env.TWITTER_CK to Env.TWITTER_CS
    val (at, ats) = Env.TWITTER_AT to Env.TWITTER_ATS
    if (ck == null || cs == null || at == null || ats == null) {
        logger.info { "Twitter の資格情報が設定されていません。Twitter 連携機能は提供しません。" }
        return@lazy null
    }

    PenicillinClient {
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

val SayaAnnictClient by lazy {
    val token = Env.ANNICT_TOKEN
    if (token == null) {
        logger.info { "Annict の資格情報が設定されていません。Annict 連携機能は提供しません。" }
        return@lazy null
    }

    AnnictClient(token)
}

val SayaMiyouTVApi by lazy {
    val (email, pass) = Env.MORITAPO_EMAIL to Env.MORITAPO_PASSWORD
    if (email == null || pass == null) {
        logger.info { "MiyouTV の資格情報が設定されていません。MiyouTV 連携機能は提供しません。" }
        return@lazy null
    }

    val login = runBlocking {
        MiyouTVApi.login(email, pass)
    }

    MiyouTVApi(login.token)
}

val SayaMirakurunApi by lazy {
    try {
        val api = MirakurunApi(Env.MIRAKURUN_HOST, Env.MIRAKURUN_PORT)

        // 接続テスト
        runBlocking {
            api.getStatus()
        }

        api
    } catch (e: Throwable) {
        logger.info(e) { "Mirakurun/mirakc に接続できません。Mirakurun/mirakc 連携機能は提供しません。" }
        null
    }
}

val SayaMirakcAribWrapper by lazy {
    if (!Files.exists(Paths.get(Env.MIRAKC_ARIB_PATH))) {
        logger.info { "mirakc-arib が見つかりません。mirakc-arib 連携機能は提供しません。" }
        return@lazy null
    }

    MirakcAribWrapper(Env.MIRAKC_ARIB_PATH)
}

val Saya5chClient by lazy {
    val (hmKey, appKey) = Env.GOCHAN_HM_KEY to Env.GOCHAN_APP_KEY
    val (authUA, ua) = Env.GOCHAN_AUTH_UA to Env.GOCHAN_UA
    val authX2chUA = Env.GOCHAN_AUTH_X_2CH_UA
    if (hmKey == null || appKey == null || authUA == null || ua == null || authX2chUA == null) {
        logger.info { "5ch API への接続情報が設定されていません。5ch 連携機能は提供しません。" }
        return@lazy null
    }

    GochanClient(hmKey, appKey, authUA, authX2chUA, ua)
}
