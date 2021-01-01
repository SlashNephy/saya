package blue.starry.saya.services.miyoutv

import blue.starry.jsonkt.parseObject
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

object MiyouTVApi {
    private const val BaseUri = "https://miteru.digitiminimi.com/a2sc.php"
    private const val UserAgent = "MiyouTV/1050201 CFNetwork/1197 Darwin/20.0.0"

    suspend fun login(email: String, password: String) = SayaHttpClient.submitForm<String>("$BaseUri/auth/moritapo", Parameters.build {
        append("email", email)
        append("password", password)
    }) {
        userAgent(UserAgent)
    }.parseObject {
        Login(it)
    }

    suspend fun getChannels(token: String) = SayaHttpClient.get<String>("$BaseUri/miyou/channels") {
        header("x-miteyou-auth-token", token)
        userAgent(UserAgent)
    }.parseObject {
        Channels(it)
    }

    suspend fun getIntervals(token: String, channel: String, startMs: Long, endMs: Long, interval: String? = null) = SayaHttpClient.get<String>("$BaseUri/miyou/intervals") {
        parameter("channel", channel)
        parameter("start", startMs)
        parameter("end", endMs)
        parameter("interval", interval ?: "1m")
        header("x-miteyou-auth-token", token)
        userAgent(UserAgent)
    }.parseObject {
        Intervals(it)
    }
}
