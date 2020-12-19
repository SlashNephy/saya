package blue.starry.saya

import kotlin.properties.ReadOnlyProperty

object Env {
    val SAYA_HOST by string
    val SAYA_PORT by int
}

private val string: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private val int: ReadOnlyProperty<Env, Int?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.toIntOrNull()
    }
