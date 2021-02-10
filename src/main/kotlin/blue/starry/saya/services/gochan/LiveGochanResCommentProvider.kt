package blue.starry.saya.services.gochan

import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.LiveCommentProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.seconds

class LiveGochanResCommentProvider(override val channel: Definitions.Channel, private val client: GochanClient): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()

    private val threadLoaders = mutableMapOf<Triple<String, String, String>, GochanDatThreadLoader>()

    private val threadSearchInterval = 5.seconds
    private val resCollectInterval = 5.seconds

    override suspend fun start() {
        joinAll(
            GlobalScope.launch {
                while (true) {
                    searchThreads()
                    delay(threadSearchInterval)
                }
            },
            GlobalScope.launch {
                while (true) {
                    collectReses()
                    delay(resCollectInterval)
                }
            }
        )
    }

    private suspend fun searchThreads() {
        AutoGochanThreadSelector.find()
    }

    private suspend fun collectReses() {

    }
}
