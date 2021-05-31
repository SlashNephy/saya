package blue.starry.saya.services.gochan

import blue.starry.saya.common.normalize
import blue.starry.saya.models.Definitions
import kotlinx.coroutines.flow.*
import org.jsoup.Jsoup
import kotlin.time.Duration
import kotlin.time.DurationUnit

object AutoPastGochanThreadSelector {
    private val threadRangePattern = "^(\\d+)-(\\d+)$".toRegex()

    suspend fun enumerate(
        client: GochanClient,
        board: Definitions.Board,
        startAt: Long,
        endAt: Long,
        allowedRange: Duration,
        limit: Int = Int.MAX_VALUE
    ) = flow {
        val keywords = board.keywords.map { it.normalize() }
        val range = allowedRange.toDouble(DurationUnit.SECONDS).toLong()

        emitAll(
            enumerateThreadList(client, board.server, board.board)
                .firstOrNull { ((startAt until endAt) intersect (it.startAt until it.endAt)).any() }
                ?.let { enumerateThreads(client, it) }
                // スレッド ID の重複を避ける
                ?.distinctUntilChangedBy { it.id }
                // スレッドキーワードが空の場合にはすべて, 空ではないならいずれかのキーワードを含むスレッドのみ
                ?.filter { keywords.isEmpty() || keywords.any { keyword -> keyword in it.title } }
                // (開始時間 - range) ~ 終了時間
                ?.filter { it.id.toLong() in (startAt - range) until endAt }
                ?.take(limit) ?: emptyFlow()
        )
    }

    private suspend fun enumerateThreadList(
        client: GochanClient,
        server: String,
        board: String,
        visitedServers: Set<String> = emptySet()
    ): Flow<GochanKakologThreadList> = flow {
        val html = client.getKakologList(server, board)
        val doc = Jsoup.parse(html)

        doc.select("p.menu_here, p.menu_link a").forEach {
            val text = it.ownText().trim()
            val href = it.attr("href")
            val match = threadRangePattern.matchEntire(text)

            // スレッドリスト
            if (match != null) {
                val (endAt, startAt) = match.destructured

                emit(GochanKakologThreadList(
                    server = server,
                    board = board,
                    filename = href?.removePrefix("./"),
                    startAt = startAt.toLong(),
                    endAt = endAt.toLong()
                ))
            // サーバリスト
            } else if (text !in visitedServers && server !in visitedServers) {
                emitAll(
                    enumerateThreadList(client, text, board, visitedServers.plus(arrayOf(server, text)))
                )
            }
        }
    }

    private suspend fun enumerateThreads(
        client: GochanClient,
        list: GochanKakologThreadList
    ) = flow {
        val html = client.getKakologList(list.server, list.board, list.filename)
        val doc = Jsoup.parse(html)

        doc.select("p.main_even, p.main_odd").reversed().forEach {
            emit(GochanKakologThread(
                list = list,
                id = it.selectFirst("span.filename").text().removeSuffix(".dat"),
                title = it.selectFirst("span.title").text(),
                url = "https://${list.server}.5ch.net${it.selectFirst("span.title a").attr("href")}",
                lines = it.selectFirst("span.lines").text().toInt()
            ))
        }
    }
}
