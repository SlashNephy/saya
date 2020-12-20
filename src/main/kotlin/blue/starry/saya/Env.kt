package blue.starry.saya

import kotlin.properties.ReadOnlyProperty

object Env {
    val SAYA_HOST by string { "0.0.0.0" }
    val SAYA_PORT by int { 1017 }
    val SAYA_BASE_URI by string { "/" }
    val ANNICT_TOKEN by string { error("Env: ANNICT_TOKEN is not set.") }
    val MIRAKURUN_HOST by string { "mirakurun" }
    val MIRAKURUN_PORT by int { 40772 }
    val CHINACHU_HOST by string { "chinachu" }
    val CHINACHU_PORT by int { 20772 }
}

private val stringOrNull: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private fun string(default: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: default()
}

private val intOrNull: ReadOnlyProperty<Env, Int?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.toIntOrNull()
    }

private fun int(default: () -> Int): ReadOnlyProperty<Env, Int> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull() ?: default()
}
