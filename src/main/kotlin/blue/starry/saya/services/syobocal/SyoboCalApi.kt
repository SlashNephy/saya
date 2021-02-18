package blue.starry.saya.services.syobocal

import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

object SyoboCalApi {
    private const val BaseUrl = "https://cal.syoboi.jp"
    private val QueryDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    private suspend inline fun <T: SyoboCalResponse<*>> query(
        command: String,
        vararg parameters: Pair<String, Any?>,
        deserializer: (Document) -> T
    ): T {
        val text = SayaHttpClient.get<String>("$BaseUrl/db.php") {
            parameter("Command", command)

            for ((key, value) in parameters) {
                parameter(key, value)
            }
        }

        val source = InputSource(StringReader(text))
        val xml = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .run {
                withContext(Dispatchers.IO) {
                    parse(source)
                }
            }

        return xml.let(deserializer)
    }

    private fun encodeLocalDateTimeRange(start: LocalDateTime?, end: LocalDateTime?): String? {
        return "${start?.format(QueryDateTimeFormatter).orEmpty()}-${end?.format(QueryDateTimeFormatter).orEmpty()}"
            .trim('-')
            .ifEmpty { null }
    }

    private fun encodeCommaList(list: List<Any>?): String? {
        return list?.joinToString(",")?.ifEmpty { null }
    }

    suspend fun lookupTitles(
        tids: List<Int> = emptyList(),
        lastUpdateStart: LocalDateTime? = null,
        lastUpdateEnd: LocalDateTime? = null
    ) = query(
        "TitleLookup",
        "TID" to tids.joinToString(",").ifEmpty { "*" },
        "LastUpdate" to encodeLocalDateTimeRange(lastUpdateStart, lastUpdateEnd),
        deserializer = TitleLookupResponse::deserialize
    )

    suspend fun lookupPrograms(
        tids: List<Int>? = null,
        chIds: List<String>? = null,
        stTimeStart: LocalDateTime? = null,
        stTimeEnd: LocalDateTime? = null,
        range: Pair<LocalDateTime, LocalDateTime>? = null,
        count: List<Int>? = null,
        lastUpdateStart: LocalDateTime? = null,
        lastUpdateEnd: LocalDateTime? = null,
        joinSubTitles: Boolean = false,
        pids: List<Int>? = null
    ) = query(
        "ProgLookup",
        "TID" to encodeCommaList(tids),
        "ChID" to encodeCommaList(chIds),
        "StTime" to encodeLocalDateTimeRange(stTimeStart, stTimeEnd),
        "Range" to range?.let { encodeLocalDateTimeRange(it.first, it.second) },
        "Count" to encodeCommaList(count),
        "LastUpdate" to encodeLocalDateTimeRange(lastUpdateStart, lastUpdateEnd),
        "JOIN" to if (joinSubTitles) "SubTitles" else null,
        "PID" to encodeCommaList(pids),
        deserializer = ProgLookupResponse::deserialize
    )

    suspend fun lookupChannels(
        chIds: List<String> = emptyList(),
        lastUpdate: String? = null
    ) = query(
        "ChLookup",
        "ChID" to encodeCommaList(chIds),
        "LastUpdate" to lastUpdate,
        deserializer = ChLookupResponse::deserialize
    )
}
