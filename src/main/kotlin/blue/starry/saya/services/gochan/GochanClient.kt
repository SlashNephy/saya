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
     * 呼び出し側のタイミングで DAT 取得を行う
     * この関数を呼び出しただけでは取得処理は開始しない
     *
     * @param server
     * @param board
     * @param threadId
     * @param additionalHeaders
     * @return
     */
    suspend fun getDat(server: String, board: String, threadId: String, additionalHeaders: Headers = Headers.Empty): HttpStatement {
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
            header(HttpHeaders.Connection, "close")
            headers.appendAll(additionalHeaders)
            expectSuccess = false
        }
    }
}
