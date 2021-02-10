package blue.starry.saya.services.nicolive

import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.LiveCommentProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel

class LiveNicoliveCommentProvider(
    override val channel: Definitions.Channel,
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()

    override suspend fun start() {
        // チャンネル名をタグ名として追加
        val tags = channel.nicoliveTags.plus(channel.name)

        tags.mapNotNull { tag ->
            val programs = NicoliveApi.getLivePrograms(tag)
            val search = programs.data.find { data ->
                // コミュニティ放送 or 「ニコニコ実況」タグ付きの公式番組
                !channel.hasOfficialNicolive || data.tags.any { it.text == "ニコニコ実況" }
            } ?: return@mapNotNull null

            val data = NicoliveApi.getEmbeddedData("https://live2.nicovideo.jp/watch/${search.id}")

            val ws = NicoliveSystemWebSocket(this@LiveNicoliveCommentProvider, data.site.relive.webSocketUrl, search.id)
            ws.start()
        }.joinAll()
    }
}
