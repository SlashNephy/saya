package blue.starry.saya.services.twitter

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.streaming.listener.FilterStreamListener
import blue.starry.penicillin.endpoints.stream
import blue.starry.penicillin.endpoints.stream.filter
import blue.starry.penicillin.models.Status
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.JikkyoChannel
import blue.starry.saya.services.LiveCommentProvider
import blue.starry.saya.services.SayaTwitterClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging

class TwitterHashTagProvider(
    override val channel: JikkyoChannel
): LiveCommentProvider {
    override val comments = BroadcastChannel<Comment>(1)
    override val subscription = LiveCommentProvider.Subscription()
    private val logger = KotlinLogging.createSayaLogger("saya.services.twitter.${channel.name}]")

    override fun start() = GlobalScope.launch {
        val client = SayaTwitterClient ?: return@launch
        val tags = channel.hashtags
        if (tags.isEmpty()) {
            return@launch
        }

        try {
            connect(client, tags)
        } catch (t: Throwable) {
            logger.trace(t) { "cancel" }
        }
    }

    private suspend fun connect(client: ApiClient, tags: Set<String>) {
        // TODO: Penicillin 側の API をあとでなおす, coroutineContext を渡す仕様に変更
        client.stream.filter(track = tags.toList()).listen(object: FilterStreamListener {
            override suspend fun onConnect() {
                logger.debug { "connect" }
            }

            override suspend fun onStatus(status: Status) {
                val comment = status.toSayaComment(tags)
                comments.send(comment)

                logger.trace { status }
            }

            override suspend fun onDisconnect(cause: Throwable?) {
                logger.debug(cause) { "disconnect" }
            }
        }, true).join()
    }
}
