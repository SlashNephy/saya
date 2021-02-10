package blue.starry.saya.services.gochan

import blue.starry.saya.models.Comment

fun GochanRes.toSayaComment(source: String): Comment {
    return Comment(
        source = source,
        time = time.toEpochSecond(),
        timeMs = time.nano / 1000_000,
        author = userId ?: name,
        text = text,
        color = "#ffffff",
        type = Comment.Position.right,
        size = Comment.Size.normal
    )
}
