package blue.starry.saya.services.gochan

import blue.starry.saya.common.normalize
import blue.starry.saya.models.Definitions

object AutoGochanThreadSelector {
    suspend fun enumerate(client: GochanClient, channel: Definitions.Channel, board: Definitions.Board, limit: Int = Int.MAX_VALUE): Sequence<GochanSubjectItem> {
        val keywords1 = board.keywords.map { it.normalize() }
        val keywords2 = channel.threadKeywords.map { it.normalize() }

        val subject = client.getSubject(board.server, board.board)
        return GochanSubjectParser.parse(subject)
            // レス数が 1000 を超えるスレッドを除外
            .filterNot { it.resCount > 1000 }
            // 運営スレッドを除外
            // ex: 9240200226.dat<>【ご案内】新型コロナウイルスについて About COVID-19 (4)
            .filterNot { it.threadId.startsWith("92") }
            // スレッドキーワードが空の場合にはすべて, 空ではないならいずれかのキーワードを含むスレッドのみ
            .filter { keywords1.isEmpty() || keywords1.any { keyword -> keyword in it.title } }
            .filter { keywords2.isEmpty() || keywords2.any { keyword -> keyword in it.title } }
            // レス数降順
            .sortedByDescending { it.resCount }
            .take(limit)
    }
}
