package blue.starry.saya.services.comments

import blue.starry.saya.common.asThreadSafe
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.Definitions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToLong

abstract class TimeshiftCommentProviderImpl(
    final override val channel: Definitions.Channel,
    final override val startAt: Long,
    final override val endAt: Long
): TimeshiftCommentProvider {
    private val logger = KotlinLogging.createSayaLogger("saya.services.timeshift[${channel.name}]")

    override val queue = Channel<Comment>(Channel.UNLIMITED)

    private val positionMs = AtomicLong(startAt * 1000)
    private val paused = AtomicBoolean(true)
    private val commentIndex = AtomicInteger()
    protected val comments = mutableListOf<Comment>().asThreadSafe()
    private var queueJob: Job? = null

    override suspend fun start() = coroutineScope {
        queueJob?.cancelAndJoin()
        queueJob = launch {
            send()
        }
    }

    private suspend fun send() {
        var timeMs = positionMs.get()
        var i = commentIndex.get()

        // コメントループ
        while (true) {
            // comments は時系列順に並んでいると仮定
            val comment = comments.withLock { it.getOrNull(i) }
            if (comment == null) {
                delay(1000)
                continue
            }

            val currentMs = comment.time * 1000 + comment.timeMs
            val waitMs = currentMs - timeMs
            delay(waitMs)

            while (paused.get()) {
                delay(100)
            }
            queue.send(comment)

            timeMs = currentMs
            i++
        }
    }

    override suspend fun seek(seconds: Double) {
        val newPositionMs = (startAt + seconds).times(1000).roundToLong()
        val newCommentIndex = comments.withLock {
            it.indexOfFirst { x -> newPositionMs < x.time * 1000 + x.timeMs }.coerceIn(0, it.size - 1)
        }

        positionMs.set(newPositionMs)
        commentIndex.set(newCommentIndex)
        queueJob?.cancel()
        logger.debug { "$this: newPositionMs = $newPositionMs, newCommentIndex = $newCommentIndex" }
    }

    override suspend fun pause() {
        paused.set(true)
    }

    override suspend fun resume() {
        paused.set(false)
    }
}
