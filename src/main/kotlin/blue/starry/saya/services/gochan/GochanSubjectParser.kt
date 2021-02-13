package blue.starry.saya.services.gochan

import blue.starry.saya.common.normalize

object GochanSubjectParser {
    private val ResCountPattern = "\\((\\d+)\\)$".toRegex()

    fun parse(text: String) = sequence {
        text.lineSequence()
            .filter { "<>" in it }
            .map { it.split("<>") }
            .filter { it.size == 2 }
            // line = 1612958146.dat<>ホンマでっか!?TV★1  (835)
            .forEach { (first, second) ->
                // first = 1612958146.dat
                // second = ホンマでっか!?TV★1  (835)

                yield(GochanSubjectItem(
                    // id = 1612958146
                    threadId = first.removeSuffix(".dat"),
                    // title = ホンマでっか!?TV★1
                    title = ResCountPattern.replace(second, "").trim().normalize(),
                    // resCount = 835
                    resCount = ResCountPattern.find(second)?.groupValues?.get(1)?.toIntOrNull()
                        ?: throw GochanParseException(GochanParseException.Type.Subject, second)
                ))
            }
    }
}
