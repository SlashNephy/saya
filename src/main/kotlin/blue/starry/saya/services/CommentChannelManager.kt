package blue.starry.saya.services

import blue.starry.saya.common.createSayaLogger
import blue.starry.saya.models.Comment
import blue.starry.saya.models.CommentSource
import blue.starry.saya.models.Definitions
import blue.starry.saya.services.gochan.LiveGochanResCommentProvider
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.nicolive.LiveNicoliveCommentProvider
import blue.starry.saya.services.twitter.LiveTwitterHashtagProvider
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
    private val definitionsPath = Paths.get("docs", "definitions.yml")

    val Channels: List<Definitions.Channel>
    val Boards: List<Definitions.Board>

    init {
        val definitions = Yaml.default.decodeFromString<Definitions>(definitionsPath.readText())
        Channels = definitions.channels
        Boards = definitions.boards
    }

    private val logger = KotlinLogging.createSayaLogger("saya.CommentChannelManager")

    suspend fun findByTarget(target: String): Definitions.Channel? {
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

    private val Providers = mutableMapOf<Pair<Definitions.Channel, CommentSource>, Pair<LiveCommentProvider, Job?>>()
    private val providersLock = Mutex()

    /**
     * リアルタイムコメント配信を購読する
     *
     * 購読数が 0 になると自動でコメントの取得 [Job] がキャンセルされる
     *
     * @param channel 実況チャンネル [Definitions.Channel]
     * @param sources コメント配信元 [CommentSource] のリスト
     */
    fun subscribeLiveComments(channel: Definitions.Channel, sources: List<CommentSource>) = GlobalScope.produce<Comment> {
        val id = UUID.randomUUID()

        /**
         * 実況チャンネル [Definitions.Channel] と コメント配信元 [CommentSource] を紐付け, コメントの取得処理を開始する
         *
         * 外側の produce が終了したときに購読数が 0 なら自動で処理も停止させる
         *
         * @param source コメント配信元 [CommentSource]
         * @param block リアルタイムコメントを取得する [LiveCommentProvider]
         */
        suspend fun register(source: CommentSource, block: () -> LiveCommentProvider?) {
            if (source !in sources) {
                return
            }

            val (provider, job) = providersLock.withLock {
                val (oldProvider, oldJob) = Providers.getOrPut(channel to source) {
                    (block() ?: return) to null
                }

                // 取得 Job
                // 前回の Job が走っていなければ再生成
                if (oldJob == null || !oldJob.isActive) {
                    Providers[channel to source] = oldProvider to GlobalScope.launch {
                        while (isActive) {
                            try {
                                oldProvider.start()
                            } catch (e: CancellationException) {
                                break
                            } catch (t: Throwable) {
                                logger.error(t) { "error in $oldProvider" }
                            }

                            delay(5.seconds)
                        }

                        logger.debug { "$oldProvider is canceled." }
                    }
                }

                Providers[channel to source]!!
            }

            // 配信 Job
            // クライアントが接続を閉じると終了する
            launch {
                try {
                    provider.subscription.create(id)
                    logger.debug { "create id: $id [${channel.name}, $source]" }

                    provider.comments.openSubscription().consumeEach {
                        send(it)
                    }
                } finally {
                    provider.subscription.remove(id)
                    logger.debug { "remove id: $id [${channel.name}, $source]" }

                    if (provider.subscription.isEmpty()) {
                        logger.debug { "There is no subscriptions on [${channel.name}, $source]. Job: $job is stopping..." }
                        job!!.cancel()
                    }

                    logger.debug { "$this is closing... ($id) [${channel.name}, $source]" }
                }
            }
        }

        register(CommentSource.Nicolive) {
            // チャンネル名をタグ名として追加
            val tags = channel.nicoliveTags.plus(channel.name)

            LiveNicoliveCommentProvider(channel, tags)
        }

        register(CommentSource.Twitter) {
            val client = SayaTwitterClient ?: return@register null
            val tags = channel.twitterKeywords.ifEmpty { return@register null }

            LiveTwitterHashtagProvider(channel, client, tags)
        }

        register(CommentSource.Gochan) {
            val client = Saya5chClient ?: return@register null
            val board = Boards.find { it.id == channel.boardId } ?: return@register null

            LiveGochanResCommentProvider(channel, client, board)
        }
    }
}
