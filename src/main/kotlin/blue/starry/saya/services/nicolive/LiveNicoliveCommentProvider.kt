package blue.starry.saya.services.nicolive

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.LiveCommentProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import mu.KotlinLogging
import kotlin.time.seconds

class LiveNicoliveCommentProvider(
    override val channel: Definitions.Channel,
    private val tags: Set<String>
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()

    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${channel.name}]")

    override suspend fun start() {
        tags.map { tag ->
            GlobalScope.launch {
                while (true) {
                    try {
                        doCollectCommentLoop(tag)
                    } catch (e: CancellationException) {
                        break
                    } catch (t: Throwable) {
                        logger.error(t) { "error in doCollectCommentLoop(${tag.intern()})" }
                    }

                    delay(5.seconds)
                }
            }
        }.joinAll()
    }

    private suspend fun doCollectCommentLoop(tag: String) {
        val programs = NicoliveApi.getLivePrograms(tag)
        val search = programs.data.find { data ->
            // コミュニティ放送 or 「ニコニコ実況」タグ付きの公式番組
            !channel.hasOfficialNicolive || data.tags.any { it.text == "ニコニコ実況" }
        } ?: return

        val data = NicoliveApi.getEmbeddedData("https://live2.nicovideo.jp/watch/${search.id}")
        val ws = NicoliveSystemWebSocket(this@LiveNicoliveCommentProvider, data.site.relive.webSocketUrl, search.id)

        ws.start()
    }
}
