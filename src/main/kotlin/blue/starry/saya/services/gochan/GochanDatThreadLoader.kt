package blue.starry.saya.services.gochan

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GochanDatThreadLoader(private val address: GochanThreadAddress) {
    private val mutex = Mutex()
    private var loadedNumBytes = 0L
    private var lastModified: String? = null

    /**
     * スレッドの差分取得を試みる
     *
     * @param client
     */
    suspend fun fetch(client: GochanClient): List<GochanRes> {
        mutex.withLock {
            val headers = Headers.build {
                if (loadedNumBytes > 0) {
                    append(HttpHeaders.Range, "bytes=${loadedNumBytes - 1}-")
                }

                if (lastModified != null) {
                    append(HttpHeaders.IfModifiedSince, lastModified!!)
                }
            }

            val response = client.getDat(address.server, address.board, address.id, headers)
            lastModified = response.headers[HttpHeaders.LastModified] ?: lastModified
            val body = response.readText(charset("MS932"))

            when (response.status) {
                // 変更なし
                HttpStatusCode.NotModified -> {
                    return emptyList()
                }
                // 全件取得
                HttpStatusCode.OK -> {
                    loadedNumBytes = response.contentLength()!!

                    return GochanDatParser.parse(body)
                }
                // 差分取得
                HttpStatusCode.PartialContent -> {
                    val range = response.headers[HttpHeaders.ContentRange]
                    if (range != null && body.isNotEmpty() && body[0] == '\n') {
                        val (startIdx, endIdx) = range.indexOf('-') to range.indexOf('/')
                        loadedNumBytes = range.slice((startIdx + 1) until endIdx).toLong() + 1

                        return GochanDatParser.parse(body.drop(1))
                    }
                }
            }

            // 差分取得に失敗
            loadedNumBytes = 0
            lastModified = null
            return fetch(client)
        }
    }
}
