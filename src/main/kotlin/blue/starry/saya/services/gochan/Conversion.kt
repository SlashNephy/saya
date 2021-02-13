package blue.starry.saya.services.gochan

import blue.starry.saya.models.Comment

fun GochanRes.toSayaComment(source: String, sourceUrl: String): Comment {
    return Comment(
        source = source,
        sourceUrl = sourceUrl,
        time = time.toEpochSecond(),
        timeMs = time.nano / 1000_000,
        author = userId ?: name,
        text = text
    )
}
