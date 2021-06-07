package blue.starry.saya.services.nicolive

import blue.starry.jsonkt.parseObject
import blue.starry.saya.services.createSayaHttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import org.jsoup.Jsoup

object NicoliveApi {
    suspend fun getLivePrograms(tag: String): SearchPrograms {
        return createSayaHttpClient().get<String>("https://api.cas.nicovideo.jp/v2/search/programs.json?liveStatus=onair&sort=startTime&limit=20&searchWord=$tag&searchTargets=tagsExact&order=desc") {
            header("X-Frontend-Id", "89")
            header("X-Model-Name", "iPhone10,1")
            header("X-Connection-Environment", "wifi")
            header("X-Request-With", "none")
            userAgent("nicocas-iOS/5.17.0 nico-webview/1.0.0")
        }.parseObject {
            SearchPrograms(it)
        }
    }

    suspend fun getEmbeddedData(url: String): EmbeddedData {
        return createSayaHttpClient().get<String>(url)
            .let {
                Jsoup.parse(it)
            }
            .getElementById("embedded-data")
            .attr("data-props")
            .replace("&quot;", "\"")
            .parseObject {
                EmbeddedData(it)
            }
    }
}
