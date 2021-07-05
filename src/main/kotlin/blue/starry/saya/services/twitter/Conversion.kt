package blue.starry.saya.services.twitter

import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.saya.models.Comment
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

fun Status.toSayaComment(source: String, tags: Set<String>): Comment {
    return Comment(
        source = "$source [${tags.joinToString(",")}]",
        sourceUrl = "https://twitter.com/${user.screenName}/status/$id",
        time = Instant.from(
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss X uuuu", Locale.ROOT).parse(createdAtRaw)
        ).epochSecond,
        timeMs = Random.nextInt(0..1000),
        author = "${user.name} @${user.screenName}",
        text = text
    )
}
