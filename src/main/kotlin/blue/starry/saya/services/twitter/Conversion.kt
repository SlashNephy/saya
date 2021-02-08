package blue.starry.saya.services.twitter

import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

fun Status.toSayaComment(tags: Set<String>): Comment {
    return Comment(
        tags.joinToString(",") { "#$it" },
        Instant.from(
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(createdAtRaw)
        ).epochSecond,
        0,
        user.name,
        tags.fold(text) { r, t -> r.replace("#$t", "") },
        "#ffffff",
        Comment.Position.right,
        Comment.Size.normal
    )
}
