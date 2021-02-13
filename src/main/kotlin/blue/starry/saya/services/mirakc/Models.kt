package blue.starry.saya.services.mirakc

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*
import blue.starry.jsonkt.string
import blue.starry.jsonkt.stringOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

// {"nid":4,"tsid":16528,"sid":211,"clock":{"pid":273,"pcr":2244665638797,"time":1607695202000}}
data class SyncClock(override val json: JsonObject): JsonModel {
    val nid by int
    val tsid by int
    val sid by int
    val clock by model { Clock(it) }

    data class Clock(override val json: JsonObject): JsonModel {
        val pid by int
        val pcr by long
        val time by long
    }
}

// {"originalNetworkId":4,"transportStreamId":16528,"serviceId":211,"tableId":80,"sectionNumber":56,"lastSectionNumber":248,"segmentLastSectionNumber":56,"versionNumber":27,"events":[{"eventId":23230,"startTime":1607691240000,"duration":360000,"scrambled":false,"descriptors":[{"$type":"ShortEvent","eventName":"耳より！Bizトレンド","text":"①高い品質とサポートで支持を集めるマウスコンピューターのノートパソコン「mouse B5」をご紹介。\n②FWD富士生命の新しいがん保険「FWDがんベスト・ゴールド」をご紹介。"},{"$type":"Component","streamContent":1,"componentType":179},{"$type":"AudioComponent","componentType":3,"samplingRate":7},{"$type":"Content","nibbles":[[2,15,15,15],[15,15,15,15]]}]},{"eventId":23231,"startTime":1607691600000,"duration":1800000,"scrambled":false,"descriptors":[{"$type":"ShortEvent","eventName":"Anison Days[アニソンデイズ]＃179◆鈴木愛奈【you／もっと高く／キミガタメ】","text":"ゲストは鈴木愛奈！ スクールアイドルグループ『Aqours』としても活躍する彼女が語るシンガー像とは？▼「ゲーム原作アニメ楽曲」に注目！ 情感あふれるカバーライブ続々！"},{"$type":"Component","streamContent":1,"componentType":179},{"$type":"AudioComponent","componentType":3,"samplingRate":7},{"$type":"Content","nibbles":[[4,0,15,15],[7,0,15,15],[4,5,15,15]]}]},{"eventId":23232,"startTime":1607693400000,"duration":1800000,"scrambled":false,"descriptors":[{"$type":"ShortEvent","eventName":"アニゲー☆イレブン！　＃268【宮本侑芽】","text":"ゲストは宮本侑芽！ 子役時代に大河ドラマにも出演の注目声優のプライベートが明らかに!?▼映画『ジョゼと虎と魚たち』に注目！ キーパーソンを演じた宮本さんの苦労とは？"},{"$type":"Component","streamContent":1,"componentType":179},{"$type":"AudioComponent","componentType":3,"samplingRate":7},{"$type":"Content","nibbles":[[5,2,15,15],[7,0,15,15],[10,6,15,15]]}]},{"eventId":23233,"startTime":1607695200000,"duration":1800000,"scrambled":false,"descriptors":[{"$type":"ShortEvent","eventName":"魔女の旅々　第11話「二人の弟子」","text":"クノーツの街を訪れたイレイナは、魔法使いを嫌悪する強盗集団「骨董堂」復活の報を耳にして、正体を隠す。一方サヤは、謎の小箱を届ける仕事の途中で妹に再会する。"},{"$type":"Component","streamContent":1,"componentType":179},{"$type":"AudioComponent","componentType":3,"samplingRate":7},{"$type":"Content","nibbles":[[7,0,15,15]]}]},{"eventId":23234,"startTime":1607697000000,"duration":1800000,"scrambled":false,"descriptors":[{"$type":"ShortEvent","eventName":"安達としまむら　第10話「桜と春と」","text":"２年生に進級した。そういえば１年前のこの時期の安達は、偶然目が合っただけなのに露骨に嫌がっていたなあ…。そして安達は、いつかの月曜から教室に姿を見せなくなった。"},{"$type":"Component","streamContent":1,"componentType":179},{"$type":"AudioComponent","componentType":3,"samplingRate":7},{"$type":"Content","nibbles":[[7,0,15,15]]}]}]}
data class CollectEit(override val json: JsonObject): JsonModel {
    val originalNetworkId by int
    val transportStreamId by int
    val serviceId by int
    val tableId by int
    val sectionNumber by int
    val lastSectionNumber by int
    val segmentLastSectionNumber by int
    val versionNumber by int
    val events by modelList { Event(it) }

    data class Event(override val json: JsonObject): JsonModel {
        val eventId by int
        val startTime by long
        val duration by int
        val scrambled by boolean
        val descriptors by modelList {
            when (val type = it["\$type"]?.stringOrNull) {
                "ShortEvent" -> Descriptor.ShortEvent(it)
                "Component" -> Descriptor.Component(it)
                "AudioComponent" -> Descriptor.AudioComponent(it)
                "Content" -> Descriptor.Content(it)
                "ExtendedEvent" -> Descriptor.ExtendedEvent(it)
                else -> TODO("$type is not implemented.")
            }
        }
    }

    sealed class Descriptor: JsonModel {
        data class ShortEvent(override val json: JsonObject): Descriptor() {
            val eventName by nullableString
            val text by nullableString
        }

        data class Component(override val json: JsonObject): Descriptor() {
            val streamContent by nullableInt
            val componentType by nullableInt
            val componentTag by nullableInt
            val languageCode by nullableInt
            val text by nullableString
        }

        data class AudioComponent(override val json: JsonObject): Descriptor() {
            val streamContent by nullableInt
            val componentType by nullableInt
            val componentTag by nullableInt
            val simulcastGroupTag by nullableInt
            val esMultiLingualFlag by nullableBoolean
            val mainComponentFlag by nullableBoolean
            val qualityIndicator by nullableInt
            val samplingRate by nullableInt
            val languageCode by nullableInt
            val languageCode2 by nullableInt
            val text by nullableString
        }

        data class Content(override val json: JsonObject): Descriptor() {
            val nibbles by lambdaList {
                it.jsonArray.map { nib -> nib.jsonPrimitive.int }
            }
        }

        data class ExtendedEvent(override val json: JsonObject): Descriptor() {
            val items by lambdaList {
                it.jsonArray.let { ext ->
                    ext.first().string to ext.last().string
                }
            }
        }
    }
}
