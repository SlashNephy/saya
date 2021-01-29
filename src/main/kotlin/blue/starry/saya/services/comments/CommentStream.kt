package blue.starry.saya.services.comments

import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.nicolive.NicoLiveApi
import kotlinx.coroutines.channels.BroadcastChannel

data class CommentStream(val channel: JikkyoChannel) {
    /**
     * コメントを配信する [BroadcastChannel]
     */
    val comments = BroadcastChannel<Comment>(1)

    private var nico: NicoLiveCommentProvider? = null
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
        val (source, data) = channel.tags.plus(channel.name).flatMap { tag ->
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

    private var twitter: TwitterHashTagProvider? = null
        get() {
            val provider = field
            return if (provider.isActive) {
                provider
            } else {
                null
            }
        }

    fun getOrCreateTwitterProvider(): TwitterHashTagProvider? {
        if (channel.hashtags.isEmpty()) {
            return null
        }

        twitter = twitter ?: TwitterHashTagProvider(this, channel.hashtags)
        return twitter
    }
}
