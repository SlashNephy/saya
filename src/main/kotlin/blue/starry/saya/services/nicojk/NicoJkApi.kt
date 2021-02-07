package blue.starry.saya.services.nicojk

import blue.starry.jsonkt.parseObject
import blue.starry.saya.models.CommentInfo
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object NicoJkApi {
    suspend fun getComments(id: String, startTime: Long, endTime: Long) = SayaHttpClient.get<String>("https://jikkyo.tsukumijima.net/api/kakolog/$id") {
        parameter("starttime", startTime)
        parameter("endtime", endTime)
        parameter("format", "json")
    }.parseObject {
        CommentLog(it)
    }

    suspend fun getChannels() = SayaHttpClient.get<String>("http://jk.from.tv/api/v2_app/getchannels").let {
        val source = InputSource(StringReader(it))
        val document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(source)

        val channels = document.getElementsByTagName("channel").asSequence() + document.getElementsByTagName("bs_channel").asSequence()

        channels.map { channel ->
            val id = channel.getElementsByTagName("id").item(0).textContent.toInt()
            val lastRes = channel.getElementsByTagName("last_res").item(0).textContent
            val force = channel.getElementsByTagName("force").item(0).textContent.toInt()

            CommentInfo(id, force, lastRes)
        }
    }

    private fun NodeList.asSequence() = sequence {
        for (i in 0 until length) {
            yield(item(i) as Element)
        }
    }
}
