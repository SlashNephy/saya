package blue.starry.saya.services

import blue.starry.saya.common.component1
import blue.starry.saya.common.component2
import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.CommentSource
import blue.starry.saya.models.Definition
import blue.starry.saya.services.gochan.GochanResCommentProvider
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.nicolive.NicoLiveCommentProvider
import blue.starry.saya.services.twitter.TwitterHashTagProvider
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.readText
import kotlin.time.seconds

object CommentChannelManager {
    private val definitionPath = Paths.get("docs", "definition.yml")

    val Channels: List<Definition.Channel>
    val Boards: List<Definition.Board>

    init {
        val definition = Yaml.default.decodeFromString<Definition>(definitionPath.readText())
        Channels = definition.channels
        Boards = definition.boards
    }

    private val logger = KotlinLogging.createSayaLogger("saya.CommentChannelManager")

    suspend fun findByTarget(target: String): Definition.Channel? {
        return if (target.startsWith("jk")) {
            // jk*** から探す

            val jk = target.removePrefix("jk").toIntOrNull() ?: return null
            Channels.find { it.nicojkId == jk }
        } else {
            // Mirakurun 互換 Service ID から探す

            val serviceId = target.toLongOrNull() ?: return null
            val mirakurun = MirakurunDataManager.Services.find { it.id == serviceId } ?: return null
            Channels.find { it.serviceIds.contains(mirakurun.actualId) }
        }
    }

    private val Providers = mutableMapOf<Pair<Definition.Channel, CommentSource>, Pair<LiveCommentProvider, Job>>()
    private val providersLock = Mutex()

    /**
     * リアルタイムコメント配信を購読する
     *
     * 購読数が 0 になると自動でコメントの取得 [Job] がキャンセルされる
     *
     * @param channel 実況チャンネル [JikkyoChannel]
     * @param sources コメント配信元 [CommentSource] のリスト
     */
    fun subscribeLiveComments(channel: Definition.Channel, sources: List<CommentSource>) = GlobalScope.produce<Comment> {
        val id = UUID.randomUUID()

        /**
         * 実況チャンネル [JikkyoChannel] と コメント配信元 [CommentSource] を紐付け, コメントの取得処理を開始する
         * 外側の produce が終了したときに購読数が 0 なら自動で処理も停止させる
         *
         * @param source コメント配信元 [CommentSource]
         * @param block リアルタイムコメントを取得する [LiveCommentProvider]
         */
        suspend fun register(source: CommentSource, block: () -> LiveCommentProvider) {
            if (source !in sources) {
                return
            }

            val (provider, job) = providersLock.withLock {
                var (first, second) = Providers[channel to source]

                // 取得 Job
                // 前回の Job が走っていなければ再生成
                if (first == null || second == null || !second.isActive) {
                    first = block()
                    second = launch {
                        while (isActive) {
                            try {
                                first.start()
                            } catch (t: Throwable) {
                                logger.error(t) { "error in $first" }
                            } finally {
                                delay(3.seconds)
                            }
                        }
                    }

                    Providers[channel to source] = first to second
                }

                first to second
            }

            // 配信 Job
            // クライアントが接続を閉じると終了する
            launch {
                provider.subscription.create(id)
                logger.debug { "create id: $id [${channel.name}, $source]" }

                provider.comments.openSubscription().consumeEach {
                    send(it)
                }
            }.invokeOnCompletion {
                runBlocking {
                    provider.subscription.remove(id)
                    logger.debug { "remove id: $id [${channel.name}, $source]" }

                    if (provider.subscription.isEmpty()) {
                        job.cancel()
                        logger.debug { "There is no subscriptions on [${channel.name}, $source]. Job: $job is stopping..." }
                    }
                }

                logger.trace(it) { "$this is closing..." }
            }
        }

        register(CommentSource.Nicolive) {
            NicoLiveCommentProvider(channel)
        }

        register(CommentSource.Twitter) {
            TwitterHashTagProvider(channel)
        }

        register(CommentSource.Gochan) {
            GochanResCommentProvider(channel)
        }
    }
}
