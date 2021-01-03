package blue.starry.saya.services.comments

import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.comments.nicolive.NicoLiveApi
import blue.starry.saya.services.comments.nicolive.NicoLiveCommentProvider
import blue.starry.saya.services.comments.twitter.TwitterHashTagProvider
import kotlinx.coroutines.channels.BroadcastChannel

data class CommentStream(val channel: JikkyoChannel) {
    /**
     * コメントを配信する [BroadcastChannel]
     */
    val comments = BroadcastChannel<Comment>(1)

    var nico: NicoLiveCommentProvider? = null
        get() {
            val provider = field
            return if (provider.isActive) {
                provider
            } else {
                null
            }
        }

    suspend fun getOrCreateNicoLiveProvider(): NicoLiveCommentProvider? {
        nico = nico ?: createNicoLiveCommentProvider()
        return nico
    }

    private suspend fun createNicoLiveCommentProvider(): NicoLiveCommentProvider? {
        // タグなし
        // TODO
        if (channel.tags.isEmpty()) {
            return null
        }

        val (source, data) = channel.tags.flatMap { tag ->
            NicoLiveApi.getLivePrograms(tag).data
                .filter { data ->
                    // 公式番組優先
                    !channel.isOfficial || data.tags.any { it.text == "ニコニコ実況" }
                }.map {
                    it.id to NicoLiveApi.getEmbeddedData("https://live2.nicovideo.jp/watch/${it.id}")
                }
        }.firstOrNull() ?: return null

        return NicoLiveCommentProvider(this, data.site.relive.webSocketUrl, source)
    }

    val twitter = mutableMapOf<String, TwitterHashTagProvider?>()

    fun getOrCreateTwitterProvider(tag: String?): TwitterHashTagProvider? {
        val key = tag ?: TwitterHashTagProvider.SampleStreamTag
        twitter[key] = twitter[key] ?: TwitterHashTagProvider(this, key)
        return twitter[key]
    }
}
