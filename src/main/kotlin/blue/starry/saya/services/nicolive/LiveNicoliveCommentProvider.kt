package blue.starry.saya.services.nicolive

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.LiveCommentProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import mu.KotlinLogging

class LiveNicoliveCommentProvider(
    override val channel: Definitions.Channel,
    private val tags: Set<String>
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()

    private val logger = KotlinLogging.createSayaLogger("saya.services.nicolive[${channel.name}]")

    override suspend fun start() = coroutineScope {
        tags.mapNotNull { tag ->
            val programs = NicoliveApi.getLivePrograms(tag)
            programs.data.find { data ->
                // コミュニティ放送 or 「ニコニコ実況」タグ付きの公式番組
                !channel.hasOfficialNicolive || data.tags.any { it.text == "ニコニコ実況" }
            }
        }.distinctBy {
            it.id
        }.map {
            NicoliveApi.getEmbeddedData("https://live2.nicovideo.jp/watch/${it.id}")
        }.map {
            launch {
                try {
                    val ws = NicoliveSystemWebSocket(this@LiveNicoliveCommentProvider, it)
                    ws.start()
                } catch (e: CancellationException) {
                } catch (t: Throwable) {
                    logger.error(t) { "error in doCollectCommentLoop(${it.program.nicoliveProgramId}, ${it.program.title})" }
                }
            }
        }.joinAll()
    }
}
