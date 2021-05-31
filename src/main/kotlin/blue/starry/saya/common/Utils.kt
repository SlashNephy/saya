package blue.starry.saya.common

import java.text.Normalizer
import java.util.*

operator fun <A, B> Pair<A, B>?.component1(): A? {
    return this?.first
}

operator fun <A, B> Pair<A, B>?.component2(): B? {
    return this?.second
}

/**
 * 文字列をあいまいに [Boolean] に変換する
 *
 *     1   -> true
 *  "true" -> true
 *   null  -> false
 *   else  -> false
 */
internal fun String?.toBooleanFuzzy(): Boolean {
    return when {
        this == null -> false
        toIntOrNull() == 1 -> true
        else -> lowercase(Locale.getDefault()).toBoolean()
    }
}

fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFKC)
}

inline fun <T> repeatMap(count: Int, block: (index: Int) -> T): List<T> {
    return (0 until count).map(block)
}
