package blue.starry.saya.services.comments

import blue.starry.saya.services.comments.nicolive.NicoLiveApi
import blue.starry.saya.services.comments.nicolive.NicoLiveCommentProvider
import blue.starry.saya.services.comments.nicolive.models.Channel
import blue.starry.saya.services.comments.twitter.TwitterHashTagProvider

data class CommentStream(val id: String, val channel: Channel) {
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

    val twitter = mutableMapOf<String, TwitterHashTagProvider?>()

    fun getOrCreateTwitterProvider(tag: String?): TwitterHashTagProvider? {
        val key = tag ?: TwitterHashTagProvider.SampleStreamTag
        twitter[key] = twitter[key] ?: TwitterHashTagProvider(this, key)
        return twitter[key]
    }
}

/**
 * アクティブな [CommentProvider] であるかどうか
 */
private val CommentProvider?.isActive: Boolean
    get() = this?.job?.isActive == true
