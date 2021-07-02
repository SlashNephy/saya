package blue.starry.saya.services.gochan

import blue.starry.saya.common.normalize

object GochanSubjectParser {
    private val pattern = "^(\\d+).dat<>(.+)\\s{2}\\((\\d+)\\)$".toRegex()

    fun parse(text: String): Sequence<GochanSubjectItem> {
        return text.lineSequence()
            .mapNotNull { pattern.matchEntire(it)?.destructured }
            // line = 1612958146.dat<>ホンマでっか!?TV★1  (835)
            .map { (threadId, title, resCount) ->
                // first = 1612958146.dat
                // second = ホンマでっか!?TV★1  (835)

                GochanSubjectItem(
                    // id = 1612958146
                    threadId = threadId,
                    // title = ホンマでっか!?TV★1
                    title = title.trim().normalize(),
                    // resCount = 835
                    resCount = resCount.toInt()
                )
            }
    }
}
