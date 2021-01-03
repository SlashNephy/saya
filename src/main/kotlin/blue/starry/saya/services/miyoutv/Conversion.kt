package blue.starry.saya.services.miyoutv

import blue.starry.saya.models.Comment
import kotlin.random.Random

fun Comments.Data.Comment.toSayaComment(no: Int): Comment {
    return Comment(
        source = title,
        no = no,
        time = time / 1000.0 + Random.nextDouble(),
        author = name,
        text = text,
        color = "#ffffff",
        type = "right",
        size = "normal",
        commands = emptyList()
    )
}
