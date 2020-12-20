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
    override var commentsPerMinute = 0
        private set

    @Transient
    private var commentsTime: Int? = null
    @Transient
    private var firstComments: Int? = null
    @Transient
    private var firstCommentsTime: Int? = null

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

        val c = comments
        val fc = firstComments ?: return
        val ct = commentsTime ?: return
        val fct = firstCommentsTime ?: return
        if (ct == fct) {
            return
        }

        commentsPerMinute = 60 * (c - fc) / (ct - fct)
    }
}
