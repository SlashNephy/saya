package blue.starry.saya.services.mirakurun

import kotlinx.serialization.json.jsonPrimitive
import java.text.Normalizer
import blue.starry.saya.models.Channel as SayaChannel
import blue.starry.saya.models.Program as SayaProgram
import blue.starry.saya.models.Service as SayaService
import blue.starry.saya.models.Tuner as SayaTuner

fun Service.toSayaService(): SayaService {
    return SayaService(
        internalId = id,
        id = serviceId,
        name = Normalizer.normalize(name, Normalizer.Form.NFKC),
        logoId = if (hasLogoData) logoId else null,
        channel = channel.channel
    )
}

suspend fun Service.Channel.toSayaChannel(): SayaChannel? {
    return SayaChannel(
        type = SayaChannel.Type.values().firstOrNull { it.name == type } ?: return null,
        group = channel,
        name = Normalizer.normalize(name.orEmpty(), Normalizer.Form.NFKC),
        serviceIds = MirakurunDataManager.Services.filter {
            it.channel == channel
        }.map {
            it.id
        }
    )
}

internal val programFlagRegex = "[【\\[(](新|終|再|字|デ|解|無|無料|二|S|SS|初|生|Ｎ|映|多|双)[】\\])]".toRegex()
fun Program.toSayaProgram(): SayaProgram {
    val name = Normalizer.normalize(name, Normalizer.Form.NFKC)

    return SayaProgram(
        id = id,
        serviceId = serviceId,
        startAt = startAt / 1000,
        duration = duration / 1000,
        name = name.replace(programFlagRegex, " "),
        description = buildString {
            appendLine(description)
            appendLine()

            extended?.forEach {
                appendLine("◇ ${it.key}\n${it.value.jsonPrimitive.content}")
            }
        }.let {
            Normalizer.normalize(it, Normalizer.Form.NFKC)
        }.trim(),
        flags = programFlagRegex.findAll(name).map { match ->
            match.groupValues[1]
        }.toList(),
        genres = genres.mapNotNull {
            it.toSayaGenre()
        }.distinct(),
        meta = SayaProgram.Meta(
            video?.type,
            video?.resolution,
            audio?.samplingRate
        )
    )
}

fun Tuner.toSayaTuner(): SayaTuner {
    return SayaTuner(
        index = index,
        name = name,
        types = types.mapNotNull { type ->
            SayaChannel.Type.values().firstOrNull { it.name == type }
        },
        command = command,
        pid = pid,
        users = users.map {
            SayaTuner.User(
                it.id,
                it.priority,
                it.agent
            )
        },
        isAvailable = isAvailable,
        isRemote = isRemote,
        isFree = isFree,
        isUsing = isUsing,
        isFault = isFault
    )
}

// Derived from https://www.arib.or.jp/kikaku/kikaku_hoso/desc/std-b10.html
internal val mainGenres = mapOf(
    0x0 to "ニュース・報道",
    0x1 to "スポーツ",
    0x2 to "情報・ワイドショー",
    0x3 to "ドラマ",
    0x4 to "音楽",
    0x5 to "バラエティ",
    0x6 to "映画",
    0x7 to "アニメ・特撮",
    0x8 to "ドキュメンタリー・教養",
    0x9 to "劇場・公演",
    0xA to "趣味・教育",
    0xB to "福祉",
    0xC to "予備",
    0xD to "予備",
    0xE to "拡張",
    0xF to "その他",
)
internal val subGenres = mapOf(
    0x0 to mapOf(
        0x0 to "定時・総合",
        0x1 to "天気",
        0x2 to "特集・ドキュメント",
        0x3 to "政治・国会",
        0x4 to "経済・市況",
        0x5 to "海外・国際",
        0x6 to "解説",
        0x7 to "討論・会談",
        0x8 to "報道特番",
        0x9 to "ローカル・地域",
        0xA to "交通",
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他"
    ),
    0x1 to mapOf(
        0x0 to "スポーツニュース",
        0x1 to "野球",
        0x2 to "サッカー",
        0x3 to "ゴルフ",
        0x4 to "その他の球技",
        0x5 to "相撲・格闘技",
        0x6 to "オリンピック・国際大会",
        0x7 to "マラソン・陸上・水泳",
        0x8 to "モータースポーツ",
        0x9 to "マリン・ウィンタースポーツ",
        0xA to "競馬・公営競技",
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x2 to mapOf(
        0x0 to "芸能・ワイドショー",
        0x1 to "ファッション",
        0x2 to "暮らし・住まい",
        0x3 to "健康・医療",
        0x4 to "ショッピング・通販",
        0x5 to "グルメ・料理",
        0x6 to "イベント",
        0x7 to "番組紹介・お知らせ",
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x3 to mapOf(
        0x0 to "国内ドラマ",
        0x1 to "海外ドラマ",
        0x2 to "時代劇",
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x4 to mapOf(
        0x0 to "国内ロック・ポップス",
        0x1 to "海外ロック・ポップス",
        0x2 to "クラシック・オペラ",
        0x3 to "ジャズ・フュージョン",
        0x4 to "歌謡曲・演歌",
        0x5 to "ライブ・コンサート",
        0x6 to "ランキング・リクエスト",
        0x7 to "カラオケ・のど自慢",
        0x8 to "民謡・邦楽",
        0x9 to "童謡・キッズ",
        0xA to "民族音楽・ワールドミュージック",
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x5 to mapOf(
        0x0 to "クイズ",
        0x1 to "ゲーム",
        0x2 to "トークバラエティ",
        0x3 to "お笑い・コメディ",
        0x4 to "音楽バラエティ",
        0x5 to "旅バラエティ",
        0x6 to "料理バラエティ",
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x6 to mapOf(
        0x0 to "洋画",
        0x1 to "邦画",
        0x2 to "アニメ",
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x7 to mapOf(
        0x0 to "国内アニメ",
        0x1 to "海外アニメ",
        0x2 to "特撮",
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x8 to mapOf(
        0x0 to "社会・時事",
        0x1 to "歴史・紀行",
        0x2 to "自然・動物・環境",
        0x3 to "宇宙・科学・医学",
        0x4 to "カルチャー・伝統文化",
        0x5 to "文学・文芸",
        0x6 to "スポーツ",
        0x7 to "ドキュメンタリー全般",
        0x8 to "インタビュー・討論",
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0x9 to mapOf(
        0x0 to "現代劇・新劇",
        0x1 to "ミュージカル",
        0x2 to "ダンス・バレエ",
        0x3 to "落語・演芸",
        0x4 to "歌舞伎・古典",
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0xA to mapOf(
        0x0 to "旅・釣り・アウトドア",
        0x1 to "園芸・ペット・手芸",
        0x2 to "音楽・美術・工芸",
        0x3 to "囲碁・将棋",
        0x4 to "麻雀・パチンコ",
        0x5 to "車・オートバイ",
        0x6 to "コンピュータ・ＴＶゲーム",
        0x7 to "会話・語学",
        0x8 to "幼児・小学生",
        0x9 to "中学生・高校生",
        0xA to "大学生・受験",
        0xB to "生涯教育・資格",
        0xC to "教育問題",
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0xB to mapOf(
        0x0 to "高齢者",
        0x1 to "障害者",
        0x2 to "社会福祉",
        0x3 to "ボランティア",
        0x4 to "手話",
        0x5 to "文字(字幕)",
        0x6 to "音声解説",
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    ),
    0xC to mapOf(
        0x0 to null,
        0x1 to null,
        0x2 to null,
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to null,
    ),
    0xD to mapOf(
        0x0 to null,
        0x1 to null,
        0x2 to null,
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to null,
    ),
    0xE to mapOf(
        0x0 to "BS/地上デジタル放送用番組付属情報",
        0x1 to "広帯域 CS デジタル放送用拡張",
        0x2 to null,
        0x3 to "サーバー型番組付属情報",
        0x4 to "IP 放送用番組付属情報",
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to null,
    ),
    0xF to mapOf(
        0x0 to null,
        0x1 to null,
        0x2 to null,
        0x3 to null,
        0x4 to null,
        0x5 to null,
        0x6 to null,
        0x7 to null,
        0x8 to null,
        0x9 to null,
        0xA to null,
        0xB to null,
        0xC to null,
        0xD to null,
        0xE to null,
        0xF to "その他",
    )
)
fun Program.Genre.toSayaGenre(): Int? {
    return lv1 * 16 + lv2
}
