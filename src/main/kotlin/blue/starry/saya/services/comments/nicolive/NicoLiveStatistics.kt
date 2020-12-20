package blue.starry.saya.services.comments.nicolive

import blue.starry.saya.services.comments.Comment
import blue.starry.saya.services.comments.CommentStatisticsProvider
import blue.starry.saya.services.comments.nicolive.models.NicoLiveWebSocketSystemJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class NicoLiveStatistics: CommentStatisticsProvider {
    var viewers = 0
        private set
    var adPoints = 0
        private set
    var giftPoints = 0
        private set
    override var comments = 0
        private set
    override val commentsPerMinute: Int
        get() {
            val c = comments
            val fc = firstComments ?: return 0
            val ct = commentsTime ?: return 0
            val fct = firstCommentsTime ?: return 0
            if (ct == fct) {
                return 0
            }

            return 60 * (c - fc) / (ct - fct).toInt()
        }

    @Transient
    private var commentsTime: Long? = null
    @Transient
    private var firstComments: Int? = null
    @Transient
    private var firstCommentsTime: Long? = null

    fun update(data: NicoLiveWebSocketSystemJson.Data) {
        viewers = data.viewers
        adPoints = data.adPoints
        giftPoints = data.giftPoints
    }

    // stats の精度がよくないのでコメントの no から計算
    fun update(comment: Comment) {
        if (firstComments == null) {
            firstComments = comment.no
        }
        if (firstCommentsTime == null) {
            firstCommentsTime = comment.time
        }

        comments = comment.no
        commentsTime = comment.time
    }
}
