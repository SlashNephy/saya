package blue.starry.saya.services.nicojk

import blue.starry.jsonkt.parseObject
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*

object NicoJkApi {
    suspend fun getComments(id: String, startTime: Long, endTime: Long) = SayaHttpClient.get<String>("https://jikkyo.tsukumijima.net/api/kakolog/$id") {
        parameter("starttime", startTime)
        parameter("endtime", endTime)
        parameter("format", "json")
    }.parseObject {
        CommentLog(it)
    }
}
