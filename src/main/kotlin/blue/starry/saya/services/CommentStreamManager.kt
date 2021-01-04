package blue.starry.saya.services

import blue.starry.saya.models.Channel
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.comments.CommentStream

object CommentStreamManager {
    val Streams = listOf(
        // GR (キー局)
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 1,
            name = "NHK 総合",
            serviceIds = setOf(
                1024,   // 関東広域: NHK総合・東京
                10240,  // 北海道(札幌): NHK総合・札幌
                11264,  // 北海道(函館): NHK総合・函館
                12288,  // 北海道(旭川): NHK総合・旭川
                13312,  // 北海道(帯広): NHK総合・帯広
                14336,  // 北海道(釧路): NHK総合・釧路
                15360,  // 北海道(北見): NHK総合・北見
                16384,  // 北海道(室蘭): NHK総合・室蘭
                17408,  // 宮城: NHK総合・仙台
                18432,  // 秋田: NHK総合・秋田
                19456,  // 山形: NHK総合・山形
                20480,  // 岩手: NHK総合・盛岡
                21504,  // 福島: NHK総合・福島
                22528,  // 青森: NHK総合・青森
                25600,  // 群馬: NHK総合・前橋
                26624,  // 茨城: NHK総合・水戸
                28672,  // 栃木: NHK総合・宇都宮
                30720,  // 長野: NHK総合・長野
                31744,  // 新潟: NHK総合・新潟
                32768,  // 山梨: NHK総合・甲府
                33792,  // 愛知: NHK総合・名古屋
                34816,  // 石川: NHK総合・金沢
                35840,  // 静岡: NHK総合・静岡
                36864,  // 福井: NHK総合・福井
                37888,  // 富山: NHK総合・富山
                38912,  // 三重: NHK総合・津
                39936,  // 岐阜: NHK総合・岐阜
                40960,  // 大阪: NHK総合・大阪
                41984,  // 京都: NHK総合・京都
                43008,  // 兵庫: NHK総合・神戸
                44032,  // 和歌山: NHK総合・和歌山
                45056,  // 奈良: NHK総合・奈良
                46080,  // 滋賀: NHK総合・大津
                47104,  // 広島: NHK総合・広島
                48128,  // 岡山: NHK総合・岡山
                49152,  // 島根: NHK総合・松江
                50176,  // 鳥取: NHK総合・鳥取
                51200,  // 山口: NHK総合・山口
                52224,  // 愛媛: NHK総合・松山
                53248,  // 香川: NHK総合・高松
                54272,  // 徳島: NHK総合・徳島
                55296,  // 高知: NHK総合・高知
                56320,  // 福岡: NHK総合・福岡
                56832,  // 福岡: NHK総合・北九州
                57344,  // 熊本: NHK総合・熊本
                58368,  // 長崎: NHK総合・長崎
                59392,  // 鹿児島: NHK総合・鹿児島
                60416,  // 宮崎: NHK総合・宮崎
                61440,  // 大分: NHK総合・大分
                62464,  // 佐賀: NHK総合・佐賀
                63488   // 沖縄: NHK総合・沖縄
            ),
            tags = setOf("NHK総合"),
            isOfficial = true,
            miyouId = "NHK総合"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 2,
            name = "Eテレ",
            serviceIds = setOf(
                1032,   // 関東広域: NHK-G
                2056,   // 近畿広域: NHKEテレ大阪
                3080,   // 中京広域: NHKEテレ名古屋
                10248,  // 北海道(札幌): NHKEテレ札幌
                11272,  // 北海道(函館): NHKEテレ函館
                12296,  // 北海道(旭川): NHKEテレ旭川
                13320,  // 北海道(帯広): NHKEテレ帯広
                14344,  // 北海道(釧路): NHKEテレ釧路
                15368,  // 北海道(北見): NHKEテレ北見
                16392,  // 北海道(室蘭): NHKEテレ室蘭
                17416,  // 宮城: NHKEテレ仙台
                18440,  // 秋田: NHKEテレ秋田
                19464,  // 山形: NHKEテレ山形
                20488,  // 岩手: NHKEテレ盛岡
                21512,  // 福島: NHKEテレ福島
                22536,  // 青森: NHKEテレ青森
                30728,  // 長野: NHKEテレ長野
                31752,  // 新潟: NHKEテレ新潟
                32776,  // 山梨: NHKEテレ甲府
                34824,  // 石川: NHKEテレ金沢
                35848,  // 静岡: NHKEテレ静岡
                36872,  // 福井: NHKEテレ福井
                37896,  // 富山: NHKEテレ富山
                47112,  // 広島: NHKEテレ広島
                48136,  // 岡山: NHKEテレ岡山
                49160,  // 島根: NHKEテレ松江
                50184,  // 鳥取: NHKEテレ鳥取
                51208,  // 山口: NHKEテレ山口
                52232,  // 愛媛: NHKEテレ松山
                53256,  // 香川: NHKEテレ高松
                54280,  // 徳島: NHKEテレ徳島
                55304,  // 高知: NHKEテレ高知
                56328,  // 福岡: NHKEテレ福岡
                56840,  // 福岡: NHKEテレ北九州
                57352,  // 熊本: NHKEテレ熊本
                58376,  // 長崎: NHKEテレ長崎
                59400,  // 鹿児島: NHKEテレ鹿児島
                60424,  // 宮崎: NHKEテレ宮崎
                61448,  // 大分: NHKEテレ大分
                62472,  // 佐賀: NHKEテレ佐賀
                63496   // 沖縄: NHKEテレ沖縄
            ),
            tags = setOf("NHK_Eテレ"),
            isOfficial = true,
            miyouId = "NHKEテレ"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 4,
            name = "日テレ",
            serviceIds = setOf(
                1040,   // 関東広域: 日テレ
                2088,   // 近畿広域: 読売テレビ
                3112,   // 中京広域: 中京テレビ
                4120,   // 北海道域: STV札幌テレビ
                5136,   // 岡山香川: RNC西日本テレビ
                6176,   // 島根鳥取: 日本海テレビ
                10264,  // 北海道(札幌): STV札幌
                11288,  // 北海道(函館): STV函館
                12312,  // 北海道(旭川): STV旭川
                13336,  // 北海道(帯広): STV帯広
                14360,  // 北海道(釧路): STV釧路
                15384,  // 北海道(北見): STV北見
                16408,  // 北海道(室蘭): STV室蘭
                17440,  // 宮城: ミヤギテレビ
                18448,  // 秋田: ABS秋田放送
                19472,  // 山形: YBC山形放送
                20504,  // 岩手: テレビ岩手
                21528,  // 福島: 福島中央テレビ
                22544,  // 青森: RAB青森放送
                30736,  // 長野: テレビ信州
                31776,  // 新潟: TeNYテレビ新潟
                32784,  // 山梨: YBS山梨放送
                34832,  // 石川: テレビ金沢
                35872,  // 静岡: だいいちテレビ
                36880,  // 福井: FBCテレビ
                37904,  // 富山: KNB北日本放送
                47128,  // 広島: 広島テレビ
                51216,  // 山口: KRY山口放送
                52240,  // 愛媛: 南海放送
                54288,  // 徳島: 四国放送
                55312,  // 高知: 高知放送
                56352,  // 福岡: FBS福岡放送
                57376,  // 熊本: KKTくまもと県民
                58408,  // 長崎: NIB長崎国際テレビ
                59432,  // 鹿児島: KYT鹿児島読売TV
                61464   // 大分: TOSテレビ大分
            ),
            tags = setOf("日本テレビ"),
            isOfficial = true,
            miyouId = "日テレ"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 5,
            name = "テレビ朝日",
            serviceIds = setOf(
                1064,   // 関東広域: テレビ朝日
                2072,   // 近畿広域: ABCテレビ
                3104,   // 中京広域: メ～テレ
                4128,   // 北海道域: HTB北海道テレビ
                5144,   // 岡山香川: KSB瀬戸内海放送
                10272,  // 北海道(札幌): HTB札幌
                11296,  // 北海道(函館): HTB函館
                12320,  // 北海道(旭川): HTB旭川
                13344,  // 北海道(帯広): HTB帯広
                14368,  // 北海道(釧路): HTB釧路
                15392,  // 北海道(北見): HTB北見
                16416,  // 北海道(室蘭): HTB室蘭
                17448,  // 宮城: KHB東日本放送
                18464,  // 秋田: AAB秋田朝日放送
                19480,  // 山形: YTS山形テレビ
                20520,  // 岩手: 岩手朝日テレビ
                21536,  // 福島: KFB福島放送
                22560,  // 青森: 青森朝日放送
                30744,  // 長野: abn長野朝日放送
                31784,  // 新潟: 新潟テレビ21
                34840,  // 石川: 北陸朝日放送
                35880,  // 静岡: 静岡朝日テレビ
                47136,  // 広島: 広島ホームテレビ
                51232,  // 山口: yab山口朝日
                52248,  // 愛媛: 愛媛朝日
                56336,  // 福岡: KBC九州朝日放送
                57384,  // 熊本: KAB熊本朝日放送
                58400,  // 長崎: NCC長崎文化放送
                59424,  // 鹿児島: KKB鹿児島放送
                61472,  // 大分: OAB大分朝日放送
                63520   // 沖縄: QAB琉球朝日放送
            ),
            tags = setOf("テレビ朝日"),
            isOfficial = true,
            miyouId = "テレビ朝日"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 6,
            name = "TBS",
            serviceIds = setOf(
                1048,   // 関東広域: TBS
                2064,   // 近畿広域: MBS毎日放送
                3096,   // 中京広域: CBC
                4112,   // 北海道域: HBC北海道放送
                5152,   // 岡山香川: RSKテレビ
                6168,   // 島根鳥取: BSSテレビ
                10256,  // 北海道(札幌): HBC札幌
                11280,  // 北海道(函館): HBC函館
                12304,  // 北海道(旭川): HBC旭川
                13328,  // 北海道(帯広): HBC帯広
                14352,  // 北海道(釧路): HBC釧路
                15376,  // 北海道(北見): HBC北見
                16400,  // 北海道(室蘭): HBC室蘭
                17424,  // 宮城: TBCテレビ
                19488,  // 山形: テレビユー山形
                20496,  // 岩手: IBCテレビ
                21544,  // 福島: テレビユー福島
                22552,  // 青森: ATV青森テレビ
                30752,  // 長野: SBC信越放送
                31760,  // 新潟: BSN
                32792,  // 山梨: UTY
                34848,  // 石川: MRO
                35856,  // 静岡: SBS
                37920,  // 富山: チューリップテレビ
                47120,  // 広島: RCCテレビ
                51224,  // 山口: tysテレビ山口
                52256,  // 愛媛: あいテレビ
                55320,  // 高知: テレビ高知
                56344,  // 福岡: RKB毎日放送
                57360,  // 熊本: RKK熊本放送
                58384,  // 長崎: NBC長崎放送
                59408,  // 鹿児島: MBC南日本放送
                60432,  // 宮崎: MRT宮崎放送
                61456,  // 大分: OBS大分放送
                63504   // 沖縄: RBCテレビ
            ),
            tags = setOf("TBSテレビ"),
            isOfficial = true,
            miyouId = "TBS"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 7,
            name = "テレビ東京",
            serviceIds = setOf(
                1072,   // 関東広域: テレビ東京
                4144,   // 北海道域: TVH
                5160,   // 岡山香川: TSCテレビせとうち
                10288,  // 北海道(札幌): TVH札幌
                11312,  // 北海道(函館): TVH函館
                12336,  // 北海道(旭川): TVH旭川
                13360,  // 北海道(帯広): TVH帯広
                14384,  // 北海道(釧路): TVH釧路
                15408,  // 北海道(北見): TVH北見
                16432,  // 北海道(室蘭): TVH室蘭
                33840,  // 愛知: テレビ愛知
                41008,  // 大阪: テレビ大阪
                56360   // 福岡: TVQ九州放送
            ),
            tags = setOf("テレビ東京"),
            isOfficial = true,
            miyouId = "テレビ東京"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 8,
            name = "フジテレビ",
            serviceIds = setOf(
                1056,   // 関東広域: フジテレビ
                2080,   // 近畿広域: 関西テレビ
                3088,   // 中京広域: 東海テレビ
                4136,   // 北海道域: UHB
                5168,   // 岡山香川: OHKテレビ
                6160,   // 島根鳥取: 山陰中央テレビ
                10280,  // 北海道(札幌): UHB札幌
                11304,  // 北海道(函館): UHB函館
                12328,  // 北海道(旭川): UHB旭川
                13352,  // 北海道(帯広): UHB帯広
                14376,  // 北海道(釧路): UHB釧路
                15400,  // 北海道(北見): UHB北見
                16424,  // 北海道(室蘭): UHB室蘭
                17432,  // 宮城: 仙台放送
                18456,  // 秋田: AKT秋田テレビ
                19496,  // 山形: さくらんぼテレビ
                20512,  // 岩手: めんこいテレビ
                21520,  // 福島: 福島テレビ
                30760,  // 長野: NBS長野放送
                31768,  // 新潟: NST
                34856,  // 石川: 石川テレビ
                35864,  // 静岡: テレビ静岡
                36888,  // 福井: 福井テレビ
                37912,  // 富山: BBT富山テレビ
                47144,  // 広島: TSS
                52264,  // 愛媛: テレビ愛媛
                55328,  // 高知: さんさんテレビ
                56368,  // 福岡: TNCテレビ西日本
                57368,  // 熊本: TKUテレビ熊本
                58392,  // 長崎: KTNテレビ長崎
                59416,  // 鹿児島: KTS鹿児島テレビ
                60440,  // 宮崎: UMKテレビ宮崎
                62480,  // 佐賀: STSサガテレビ
                63544   // 沖縄: 沖縄テレビ(OTV)
            ),
            tags = setOf("フジテレビ"),
            isOfficial = true,
            miyouId = "フジテレビ"
        ),

        // GR (地方局)
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 9,
            name = "TOKYO MX",
            serviceIds = setOf(
                23608,  // 東京: TOKYO MX1
                23610   // 東京: TOKYO MX2
            ),
            tags = setOf("TOKYO_MX"),
            isOfficial = true,
            miyouId = "TOKYO MX"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 231,
            name = "放送大学",
            serviceIds = setOf(
                1088,  // 関東広域: 放送大学
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 10,
            name = "テレ玉",
            serviceIds = setOf(
                29752  // 埼玉: テレ玉
            ),
            tags = setOf("テレ玉"),
            miyouId = "テレ玉"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 11,
            name = "tvk",
            serviceIds = setOf(
                24632  // 神奈川: tvk
            ),
            tags = setOf("tvk"),
            miyouId = "tvk"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            jk = 12,
            name = "チバテレビ",
            serviceIds = setOf(
                27704  // 千葉: チバテレビ
            ),
            tags = setOf("チバテレビ"),
            miyouId = "チバテレ"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "群馬テレビ",
            serviceIds = setOf(
                25656  // 群馬: 群馬テレビ
            ),
            miyouId = "群馬テレビ"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "とちぎテレビ",
            serviceIds = setOf(
                28728  // 栃木: とちぎテレビ
            ),
            miyouId = "とちぎテレビ"
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "三重テレビ",
            serviceIds = setOf(
                38960  // 三重: 三重テレビ
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "ぎふチャン",
            serviceIds = setOf(
                39984  // 岐阜: ぎふチャン
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "KBS京都",
            serviceIds = setOf(
                42032  // 京都: KBS京都
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "サンテレビ",
            serviceIds = setOf(
                43056  // 兵庫: サンテレビ
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "テレビ和歌山",
            serviceIds = setOf(
                44080  // 和歌山: テレビ和歌山
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "奈良テレビ",
            serviceIds = setOf(
                45104  // 奈良: 奈良テレビ
            )
        ),
        JikkyoChannel(
            type = Channel.Type.GR,
            name = "BBCびわ湖放送",
            serviceIds = setOf(
                46128  // 滋賀: BBCびわ湖放送
            )
        ),

        // BS
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 101,
            name = "NHKBS1",
            serviceIds = setOf(101, 102),
            tags = setOf("NHKBS-1", "NHKBS", "NHK_BS"),
            miyouId = "NHKBS1"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 103,
            name = "NHKBSプレミアム",
            serviceIds = setOf(103, 104),
            tags = setOf("NHK_BSプレミアム", "NHKBSプレミアム", "BSプレミアム"),
            miyouId = "NHKBSプレミアム"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 141,
            name = "BS日テレ",
            serviceIds = setOf(141, 142, 143),
            tags = setOf("BS_日テレ", "BS日テレ"),
            miyouId = "BS日テレ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 151,
            name = "BS朝日",
            serviceIds = setOf(151, 152, 153),
            tags = setOf("BS_朝日", "BS朝日"),
            miyouId = "BS朝日"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 161,
            name = "BS-TBS",
            serviceIds = setOf(161, 162, 163),
            tags = setOf("BS-TBS", "BSTBS"),
            miyouId = "BS-TBS"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 171,
            name = "BSテレ東",
            serviceIds = setOf(171, 172, 173),
            tags = setOf("BSジャパン", "BSテレ東"),
            miyouId = "BSジャパン"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 181,
            name = "BSフジ",
            serviceIds = setOf(181, 182, 183),
            tags = setOf("BSフジ"),
            miyouId = "BSフジ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 191,
            name = "WOWOWプライム",
            serviceIds = setOf(191),
            miyouId = "WOWOWプライム"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 192,
            name = "WOWOWライブ",
            serviceIds = setOf(192),
            miyouId = "WOWOWライブ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 193,
            name = "WOWOWシネマ",
            serviceIds = setOf(193),
            miyouId = "WOWOWシネマ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 200,
            name = "スターチャンネル1",
            serviceIds = setOf(200),
            miyouId = "スターチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 201,
            name = "スターチャンネル2",
            serviceIds = setOf(201),
            miyouId = "スターチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 202,
            name = "スターチャンネル3",
            serviceIds = setOf(202),
            miyouId = "スターチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 211,
            name = "BS11イレブン",
            tags = setOf("BS11"),
            isOfficial = true,
            serviceIds = setOf(211),
            miyouId = "BS11イレブン"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 222,
            name = "BS12トゥエルビ",
            tags = setOf("TwellV"),
            serviceIds = setOf(222),
            miyouId = "BS12トゥエルビ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 231,
            name = "放送大学ex",
            serviceIds = setOf(231)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 231,
            name = "放送大学on",
            serviceIds = setOf(232)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 234,
            name = "グリーンチャンネル",
            serviceIds = setOf(234),
            tags = setOf("グリーンチャンネル"),
            miyouId = "グリーンチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 236,
            name = "BSアニマックス",
            serviceIds = setOf(236),
            miyouId = "BSアニマックス"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 238,
            name = "FOXbs238",
            serviceIds = setOf(238),
            miyouId = "FOXスポーツエンタ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 241,
            name = "BSスカパー!",
            serviceIds = setOf(241),
            miyouId = "BSスカパー"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 242,
            name = "J SPORTS 1",
            serviceIds = setOf(242),
            miyouId = "J SPORTS"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 243,
            name = "J SPORTS 2",
            serviceIds = setOf(243),
            miyouId = "J SPORTS"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 244,
            name = "J SPORTS 3",
            serviceIds = setOf(244),
            miyouId = "J SPORTS"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 245,
            name = "J SPORTS 4",
            serviceIds = setOf(245),
            miyouId = "J SPORTS"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 251,
            name = "BS釣りビジョン",
            serviceIds = setOf(251),
            miyouId = "BS釣りビジョン"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 252,
            name = "WOWOWプラス",
            serviceIds = setOf(252),
            miyouId = "イマジカBS・映画"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 255,
            name = "日本映画専門ch",
            serviceIds = setOf(255),
            miyouId = "BS日本映画専門"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 256,
            name = "ディズニーch",
            serviceIds = setOf(256),
            miyouId = "ディズニーチャンネル"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 258,
            name = "Dlife",
            serviceIds = setOf(258),
            miyouId = "ディーライフ"
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "放送大学ラジオ",
            serviceIds = setOf(531)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "NHKデータ1",
            serviceIds = setOf(700, 701)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "707チャンネル",
            serviceIds = setOf(707)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "株価情報",
            serviceIds = setOf(777)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "ご案内チャンネル",
            serviceIds = setOf(791)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "プレミアムナビ",
            serviceIds = setOf(792)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "スターチャンネル",
            serviceIds = setOf(800)
        ),
        JikkyoChannel(
            type = Channel.Type.BS,
            name = "スカパー!ガイド",
            serviceIds = setOf(840)
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.BS,
            jk = 910,
            name = "SOLiVE24",
            serviceIds = setOf(910)
        ),

        // CS: 映画
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "東映チャンネル",
            serviceIds = setOf(218),
            miyouId = "東映チャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "衛星劇場",
            serviceIds = setOf(219),
            miyouId = "衛星劇場"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "映画・チャンネルNECO",
            serviceIds = setOf(223),
            miyouId = "映画・chNECO"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ザ・シネマ",
            serviceIds = setOf(227),
            miyouId = "ザ・シネマ"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "FOXムービー",
            serviceIds = setOf(229)
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ムービープラス",
            serviceIds = setOf(240),
            miyouId = "ムービープラス"
        ),

        // スポーツ
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカイA",
            serviceIds = setOf(250),
            miyouId = "スカイA"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "GAORA SPORTS",
            serviceIds = setOf(254),
            miyouId = "GAORA"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "日テレジータス",
            serviceIds = setOf(257),
            miyouId = "日テレジータス"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ゴルフネットワーク",
            serviceIds = setOf(262),
            miyouId = "ゴルフネット"
        ),

        // CS: 音楽
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スペースシャワーTV プラス",
            serviceIds = setOf(321),
            miyouId = "スペシャプラス"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スペースシャワーTV",
            serviceIds = setOf(322),
            miyouId = "スペースシャワーTV"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "MTV",
            serviceIds = setOf(323),
            miyouId = "MTV"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ミュージック・エア",
            serviceIds = setOf(
                324,
                326  // 停波済み
            ),
            miyouId = "ミュージック・エア"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "エムオン!",
            serviceIds = setOf(325),
            miyouId = "エムオン"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "歌謡ポップス",
            serviceIds = setOf(329),
            miyouId = "歌謡ポップス"
        ),

        // CS: アニメ
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "キッズステーション",
            serviceIds = setOf(330),
            miyouId = "キッズステーション"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "カートゥーン ネットワーク",
            serviceIds = setOf(331),
            miyouId = "カートゥーン"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            jk = 333,
            name = "AT-X",
            serviceIds = setOf(333),
            tags = setOf("AT-X"),
            miyouId = "AT−X"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ディズニーXD",
            serviceIds = setOf(334)
        ),

        // CS: 総合エンターテイメント
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "TBSチャンネル1",
            serviceIds = setOf(296),
            miyouId = "TBSチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "TBSチャンネル2",
            serviceIds = setOf(297),
            miyouId = "TBSチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "テレ朝チャンネル1",
            serviceIds = setOf(298),
            miyouId = "テレ朝チャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "テレ朝チャンネル2",
            serviceIds = setOf(299)
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "日テレプラス",
            serviceIds = setOf(300),
            miyouId = "日テレプラス"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "エンタメ～テレ☆シネドラバラエティ",
            serviceIds = setOf(301)
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "チャンネル銀河",
            serviceIds = setOf(305),
            miyouId = "銀河"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "フジテレビONE",
            serviceIds = setOf(307),
            miyouId = "フジテレビONE"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "フジテレビTWO",
            serviceIds = setOf(308),
            miyouId = "フジテレビTWO"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "フジテレビNEXT",
            serviceIds = setOf(309),
            miyouId = "フジテレビNEXT"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカチャン0",
            serviceIds = setOf(800),
            miyouId = "スカサカ"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカチャン1",
            serviceIds = setOf(801),
            miyouId = "スカチャン"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカチャン2",
            serviceIds = setOf(802),
            miyouId = "スカチャン"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカチャン3",
            serviceIds = setOf(805),
            miyouId = "スカチャン"
        ),

        // CS: 海外ドラマ・バラエティ
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スーパー！ドラマTV",
            serviceIds = setOf(310),
            miyouId = "スーパードラマ"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "AXN",
            serviceIds = setOf(311),
            miyouId = "AXN 海外ドラマ"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "FOX",
            serviceIds = setOf(312),
            miyouId = "FOX"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "女性チャンネル♪LaLa TV",
            serviceIds = setOf(314),
            miyouId = "女性ch/LaLa"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "FOXプラス",
            serviceIds = setOf(315)
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "AXNミステリー",
            serviceIds = setOf(316),
            miyouId = "AXNミステリー"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "KBS World",
            serviceIds = setOf(317)
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "M net",
            serviceIds = setOf(318)
        ),

        // CS: 国内ドラマ・バラエティ・舞台
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "TAKARAZUKA SKY STAGE",
            serviceIds = setOf(290),
            miyouId = "SKY STAGE"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "時代劇専門チャンネル",
            serviceIds = setOf(292),
            miyouId = "時代劇専門"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ファミリー劇場",
            serviceIds = setOf(293),
            miyouId = "ファミリー劇場"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ホームドラマチャンネル",
            serviceIds = setOf(294),
            miyouId = "ホームドラマ"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "MONDO TV",
            serviceIds = setOf(295),
            miyouId = "MONDO TV"
        ),
        // 停波済み
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "旅チャンネル",
            serviceIds = setOf(362)
        ),

        // CS: ドキュメンタリー
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ディスカバリーチャンネル",
            serviceIds = setOf(340),
            miyouId = "ディスカバリー"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "アニマルプラネット",
            serviceIds = setOf(341),
            miyouId = "アニマルプラネット"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ヒストリーチャンネル",
            serviceIds = setOf(342),
            miyouId = "ヒストリーチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ナショナル ジオグラフィック",
            serviceIds = setOf(343),
            miyouId = "ナショジオ"
        ),

        // CS: ニュース
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "日テレNEWS24",
            serviceIds = setOf(349),
            miyouId = "日テレNEWS24"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "TBS NEWS",
            serviceIds = setOf(351),
            miyouId = "TBSニュースバード"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "BBCワールドニュース",
            serviceIds = setOf(353),
            miyouId = "BBCワールド"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "CNNj",
            serviceIds = setOf(354),
            miyouId = "CNNj"
        ),

        // CS: 教育
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ディズニージュニア",
            serviceIds = setOf(339),
            miyouId = "ディズニージュニア"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "囲碁・将棋チャンネル",
            serviceIds = setOf(363),
            miyouId = "囲碁・将棋チャンネル"
        ),

        // CS: ショッピング
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "ショップチャンネル",
            serviceIds = setOf(55),
            miyouId = "ショップチャンネル"
        ),
        JikkyoChannel(
            type = Channel.Type.CS,
            name = "QVC",
            serviceIds = setOf(161),
            miyouId = "QVC"
        ),

        JikkyoChannel(
            type = Channel.Type.CS,
            name = "スカパー!プロモ",
            serviceIds = setOf(100),
            miyouId = "スカパープロモ"
        )
    ).map {
        CommentStream(it)
    }
}
