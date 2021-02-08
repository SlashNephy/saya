package blue.starry.saya.services.twitter

import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

fun Status.toSayaComment(tags: Set<String>): Comment? {
    if (text.startsWith("RT @")) {
        return null
    }

    return Comment(
        "Twitter [${tags.joinToString(",") { "#$it" }}]",
        Instant.from(
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(createdAtRaw)
        ).epochSecond,
        Random.nextInt(0..1000),
        "${user.name} @${user.screenName}",
        tags.fold(text) { r, t -> r.replace("#$t", "") },
        "#ffffff",
        Comment.Position.right,
        Comment.Size.normal
    )
}
