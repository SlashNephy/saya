package blue.starry.saya.services.miyoutv

import blue.starry.saya.models.Comment
import kotlin.random.Random

// TODO: time
fun Comments.Data.Comment.toSayaComment(): Comment {
    return Comment(
        source = title,
        time = time / 1000.0 + Random.nextDouble(),
        author = name,
        text = text,
        color = "#ffffff",
        type = Comment.Position.right,
        size = Comment.Size.normal
    )
}
