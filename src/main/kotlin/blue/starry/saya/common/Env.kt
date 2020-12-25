package blue.starry.saya.common

import kotlin.properties.ReadOnlyProperty

object Env {
    val SAYA_HOST by string { "0.0.0.0" }
    val SAYA_PORT by int { 1017 }
    val SAYA_BASE_URI by string { "/" }
    val SAYA_TMP_DIR by string { "tmp" }
    val SAYA_HLS_SEGMENT_SEC by int { 2 }
    val SAYA_HLS_SEGMENT_SIZE by int { 3 }
    val ANNICT_TOKEN by string
    val TWITTER_CK by string
    val TWITTER_CS by string
    val TWITTER_AT by string
    val TWITTER_ATS by string
    val MIRAKURUN_HOST by string { "mirakurun" }
    val MIRAKURUN_PORT by int { 40772 }
    val CHINACHU_HOST by string { "chinachu" }
    val CHINACHU_PORT by int { 20772 }
    val EPGSTATION_HOST by string { "epgstation" }
    val EPGSTATION_PORT by int { 8888 }
}

private val string: ReadOnlyProperty<Env, String>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name) ?: error("Env: ${property.name} is not set.")
    }

private fun string(default: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: default()
}

private val int: ReadOnlyProperty<Env, Int>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.toIntOrNull() ?: error("Env: ${property.name} is not set.")
    }

private fun int(default: () -> Int): ReadOnlyProperty<Env, Int> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull() ?: default()
}
