package blue.starry.saya.services.comments

import blue.starry.saya.services.comments.nicolive.NicoLiveApi
import blue.starry.saya.services.comments.nicolive.NicoLiveCommentProvider
import blue.starry.saya.services.comments.nicolive.models.Channel

data class CommentStream(val id: String, val channel: Channel) {
    private var nicoLiveCommentProvider: NicoLiveCommentProvider? = null

    suspend fun getNicoLiveCommentProvider(creating: Boolean = true): NicoLiveCommentProvider? {
        val p = nicoLiveCommentProvider
        // アクティブな CommentProvider が存在するとき
        if (p != null && p.job.isActive) {
            return p
        }

        if (!creating) {
            return null
        }

        nicoLiveCommentProvider = createNicoLiveCommentProvider()
        return nicoLiveCommentProvider
    }

    private suspend fun createNicoLiveCommentProvider(): NicoLiveCommentProvider? {
        // タグなし
        if (channel.tags.isEmpty()) {
            return null
        }

        val data = channel.tags.flatMap { tag ->
            NicoLiveApi.getLivePrograms(tag).data
                .filter { data ->
                    // 公式番組優先
                    !channel.official || data.tags.any { it.text == "ニコニコ実況" }
                }.map {
                    "https://live2.nicovideo.jp/watch/${it.id}"
                }.map {
                    NicoLiveApi.getEmbeddedData(it)
                }
        }.firstOrNull() ?: return null

        return NicoLiveCommentProvider(this, data.site.relive.webSocketUrl)
    }

    private fun closeProvider() {
        nicoLiveCommentProvider?.job?.cancel()
    }

    private var subscriptions = 0

    /**
     * ストリームの購読数をチェックし自動で [CommentProvider] を閉じる
     */
    suspend fun withSession(block: suspend () -> Unit) {
        try {
            subscriptions++

            block()
        } finally {
            if (--subscriptions <= 0) {
                subscriptions = 0
                closeProvider()
            }
        }
    }
}
