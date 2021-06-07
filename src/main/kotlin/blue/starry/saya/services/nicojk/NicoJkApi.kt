package blue.starry.saya.services.nicojk

import blue.starry.jsonkt.parseObject
import blue.starry.saya.models.CommentInfo
import blue.starry.saya.services.comments.CommentChannelManager
import blue.starry.saya.services.createSayaHttpClient
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object NicoJkApi {
    suspend fun getComments(id: String, startTime: Long, endTime: Long) = createSayaHttpClient().get<String>("https://jikkyo.tsukumijima.net/api/kakolog/$id") {
        parameter("starttime", startTime)
        parameter("endtime", endTime)
        parameter("format", "json")
    }.parseObject {
        CommentLog(it)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getChannels() = createSayaHttpClient().get<String>("http://jk.from.tv/api/v2_app/getchannels").let {
        val source = InputSource(StringReader(it))
        val document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .run {
                withContext(Dispatchers.IO) {
                    parse(source)
                }
            }

        document.getElementsByTagName("channel").toList() + document.getElementsByTagName("bs_channel").toList()
    }.let { channels ->
        channels.map { channel ->
            val jk = channel.getFirstElementByTagName("id").textContent.toInt()
            val jkChannel = CommentChannelManager.Channels.first { it.nicojkId == jk }

            CommentInfo(
                channel = jkChannel,
                force = channel.getFirstElementByTagName("force").textContent.toInt(),
                last = channel.getFirstElementByTagName("last_res").textContent
            )
        }
    }

    private fun NodeList.toList() = buildList {
        for (i in 0 until length) {
            add(item(i) as Element)
        }
    }

    private fun Element.getFirstElementByTagName(name: String): Node {
        return getElementsByTagName(name).item(0)
    }
}
