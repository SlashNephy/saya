package blue.starry.saya.services.miyoutv

import blue.starry.saya.models.Comment
import kotlin.random.Random
import kotlin.random.nextInt

fun Comments.Data.Comment.toSayaComment(seconds: Double): Comment {
    return Comment(
        source = "MiyouTV [$title]",
        time = time / 1000,
        timeMs = Random.nextInt(0..1000),
        seconds = seconds,
        author = "$name ($id)",
        text = text.trim()
    )
}
