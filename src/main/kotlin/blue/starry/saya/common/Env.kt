package blue.starry.saya.common

import kotlin.properties.ReadOnlyProperty

object Env {
    val SAYA_HOST by string { "0.0.0.0" }
    val SAYA_PORT by int { 1017 }
    val SAYA_BASE_URI by string { "/" }
    val SAYA_LOG by string { "INFO" }
    val SAYA_UPDATE_INTERVAL_MINS by int { 15 }

    val MIRAKURUN_HOST by string { "mirakurun" }
    val MIRAKURUN_PORT by int { 40772 }
    val ANNICT_TOKEN by stringOrNull
    val TWITTER_CK by stringOrNull
    val TWITTER_CS by stringOrNull
    val TWITTER_AT by stringOrNull
    val TWITTER_ATS by stringOrNull
    val TWITTER_PREFER_STREAMING_API by boolean { false }
    val MORITAPO_EMAIL by stringOrNull
    val MORITAPO_PASSWORD by stringOrNull
    val MOUNT_POINT by stringOrNull
    val MIRAKC_ARIB_PATH by string { "/usr/local/bin/mirakc-arib" }
    val FFMPEG_PATH by string { "/usr/local/bin/ffmpeg" }
}

private fun boolean(default: () -> Boolean): ReadOnlyProperty<Env, Boolean> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toBooleanFuzzy() ?: default()
}

private val stringOrNull: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private fun string(default: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: default()
}

private fun int(default: () -> Int): ReadOnlyProperty<Env, Int> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull() ?: default()
}
