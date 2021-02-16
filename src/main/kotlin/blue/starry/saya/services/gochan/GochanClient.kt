package blue.starry.saya.services.gochan

import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils

class GochanClient(
    private val hmKey: String,
    private val appKey: String,
    private val authUA: String,
    private val authX2chUA: String,
    private val ua: String
) {
    private var sessionId: String? = null

    private fun calculateHash(message: String): String {
        return HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmKey).hmacHex(message)
    }

    private suspend fun authorize() {
        val parameters = Parameters.build {
            append("KY", appKey)
            append("ID", "")
            append("PW", "")
        }
        val response = SayaHttpClient.submitForm<String>("https://api.5ch.net/v1/auth/", parameters) {
            userAgent(authUA)
            header("X-2ch-UA", authX2chUA)
        }

        if (':' !in response) {
            error("5ch API への認証に失敗しました。")
        }

        sessionId = response.split(':').last()
    }

    /**
     * DAT 取得を行う
     */
    suspend fun getDat(server: String, board: String, threadId: String, additionalHeaders: Headers = Headers.Empty): HttpResponse {
        if (sessionId == null) {
            authorize()
        }

        val message = "/v1/$server/$board/$threadId$sessionId$appKey"
        val hobo = calculateHash(message)

        val parameters = Parameters.build {
            append("sid", sessionId!!)
            append("hobo", hobo)
            append("appkey", appKey)
        }

        return SayaHttpClient.submitForm("https://api.5ch.net/v1/$server/$board/$threadId", parameters) {
            userAgent(ua)
            headers.appendAll(additionalHeaders)
            expectSuccess = false
        }
    }

    /**
     * スレッド一覧を取得する
     */
    suspend fun getSubject(server: String, board: String): String {
        return SayaHttpClient.get<HttpResponse>("https://$server.5ch.net/$board/subject.txt") {
            userAgent(ua)
        }.readText(charset("MS932"))
    }

    suspend fun get2chScDat(server: String, board: String, threadId: String): String {
        return SayaHttpClient.get("https://$server.2ch.sc/$board/dat/$threadId.dat") {
            userAgent(ua)
        }
    }

    suspend fun getKakologList(server: String, board: String, filename: String? = null): String {
        return SayaHttpClient.get("https://$server.5ch.net/$board/kako/${filename.orEmpty()}") {
            userAgent(ua)
        }
    }
}
