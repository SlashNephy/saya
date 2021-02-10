package blue.starry.saya.services.gochan

import java.time.ZoneId
import java.time.ZonedDateTime

object GochanDatParser {
    fun parse(text: String): List<GochanRes> {
        return text.trim()
            .lines()
            .map { it.split("<>") }
            .filter { it.size == 5 }
            .map { res ->
                GochanRes(
                    name = removeHtml(res[0]),
                    mail = removeHtml(res[1]),
                    userId = parseUserId(res[2]),
                    time = parseDate(res[2]),
                    text = removeHtml(res[3])
                )
            }
    }

    private val DatePattern = "(\\d+)/(\\d+)/(\\d+)[^ ]* (\\d+):(\\d+):(\\d+)\\.(\\d+)".toRegex()
    private val UserIdParrern = "ID:(.+)".toRegex()
    private val TagPattern = "</?[\\w\\s\"./=]+?>".toRegex()

    private fun parseDate(text: String): ZonedDateTime {
        val match = DatePattern.find(text) ?: throw GochanParseException(GochanParseException.Type.Dat, text)
        val (year, month, day, hour, minute, second, ms) = match.destructured

        return ZonedDateTime.of(
            year.toInt(), month.toInt(), day.toInt(),
            hour.toInt(), minute.toInt(), second.toInt(), ms.toInt() * 10_000_000,
            ZoneId.of("Asia/Tokyo")
        )
    }

    private fun parseUserId(text: String): String? {
        val match = UserIdParrern.find(text) ?: return null

        return match.groupValues[1]
    }

    private fun removeHtml(text: String): String {
        return text.replace("<br>", "\n")
            .replace("&gt;", ">")
            .replace("&lt;", "<")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace(TagPattern, "")
            .trim()
    }
}
