package blue.starry.saya.common

import kotlin.properties.ReadOnlyProperty

object Env {
    val SAYA_HOST by string { "0.0.0.0" }
    val SAYA_PORT by int { 1017 }
    val SAYA_BASE_URI by string { "/" }
    val SAYA_LOG by string { "INFO" }

    val ANNICT_TOKEN by stringOrNull
    val TWITTER_CK by stringOrNull
    val TWITTER_CS by stringOrNull
    val TWITTER_AT by stringOrNull
    val TWITTER_ATS by stringOrNull
    val TWITTER_PREFER_STREAMING_API by boolean { true }
    val GOCHAN_HM_KEY by stringOrNull
    val GOCHAN_APP_KEY by stringOrNull
    val GOCHAN_AUTH_UA by stringOrNull
    val GOCHAN_AUTH_X_2CH_UA by stringOrNull
    val GOCHAN_UA by stringOrNull
    val MORITAPO_EMAIL by stringOrNull
    val MORITAPO_PASSWORD by stringOrNull
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
