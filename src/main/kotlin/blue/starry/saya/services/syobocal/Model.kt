@file:Suppress("Unused")

package blue.starry.saya.services.syobocal

import blue.starry.saya.common.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.ReadOnlyProperty

interface SyoboCalResponse<T: Any>: List<T> {
    val result: Result
    val items: List<T>

    data class Result(
        val code: Int,
        val message: String?
    )
}

private val Document.result: SyoboCalResponse.Result
    get() {
        val element = getFirstElementByTagName("Result")!!

        return SyoboCalResponse.Result(
            code = element.getFirstElementByTagName("Code")!!.textContent.toInt(),
            message = element.getFirstElementByTagName("Message")!!.textContent.ifEmpty { null }
        )
    }

private val syoboCalDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private fun XmlModel.localDateTime(tagName: String? = null): ReadOnlyProperty<XmlModel, LocalDateTime> = delegate {
    LocalDateTime.parse(it.textContent, syoboCalDateTimeFormat)
}

data class TitleLookupResponse(
    override val items: List<TitleItem>,
    override val result: SyoboCalResponse.Result,
): SyoboCalResponse<TitleLookupResponse.TitleItem>, List<TitleLookupResponse.TitleItem> by items {
    data class TitleItem(override val xml: Element): XmlModel {
        val tid by int("TID")
        val lastUpdate by localDateTime()
        val title by string()
        val shortTitle by stringOrNull()
        val titleYomi by string()
        val titleEn by stringOrNull("TitleEN")
        val comment by string()
        val cat by int()
        val titleFlag by int()
        val firstYear by int()
        val firstMonth by int()
        val firstEndYear by intOrNull()
        val firstEndMonth by intOrNull()
        val firstCh by stringOrNull()
        val keywords by delegate {
            it.textContent.split(",")
        }
        val userPoint by int()
        val userPointRank by int()
        val subTitles by stringOrNull()
    }

    companion object {
        fun deserialize(xml: Document): TitleLookupResponse {
            return TitleLookupResponse(
                items = xml.getElementsByTagName("TitleItem").map { TitleItem(it) },
                result = xml.result
            )
        }
    }
}

data class ProgLookupResponse(
    override val items: List<ProgItem>,
    override val result: SyoboCalResponse.Result,
): SyoboCalResponse<ProgLookupResponse.ProgItem>, List<ProgLookupResponse.ProgItem> by items {
    data class ProgItem(override val xml: Element): XmlModel {
        val lastUpdate: LocalDateTime by localDateTime()
        val pid by int("PID")
        val tid by int("TID")
        val stTime: LocalDateTime by localDateTime()
        val stOffset by int()
        val edTime: LocalDateTime by localDateTime()
        val count by int()
        val subTitle by stringOrNull()
        val progComment by stringOrNull()
        val flag by int()
        val deleted by delegate {
            it.textContent == "1"
        }
        val warn by delegate {
            it.textContent == "1"
        }
        val chId by int("ChID")
        val revision by int()
        val stSubTitle by stringOrNull("STSubTitle")
    }

    companion object {
        fun deserialize(xml: Document): ProgLookupResponse {
            return ProgLookupResponse(
                items = xml.getElementsByTagName("ProgItem").map { ProgItem(it) },
                result = xml.result
            )
        }
    }
}

data class ChLookupResponse(
    override val items: List<ChItem>,
    override val result: SyoboCalResponse.Result
): SyoboCalResponse<ChLookupResponse.ChItem>, List<ChLookupResponse.ChItem> by items {
    data class ChItem(override val xml: Element): XmlModel {
        val lastUpdate by localDateTime()
        val chId by int("ChID")
        val chName by string()
        val chEpgName by stringOrNull("ChiEPGName")
        val chUrl by stringOrNull("ChURL")
        val chEpgUrl by stringOrNull("ChEPGURL")
        val chComment by stringOrNull()
        val chGid by int("ChGID")
        val chNumber by intOrNull()
    }

    companion object {
        fun deserialize(xml: Document): ChLookupResponse {
            return ChLookupResponse(
                items = xml.getElementsByTagName("ChItem").map { ChItem(it) },
                result = xml.result
            )
        }
    }
}
