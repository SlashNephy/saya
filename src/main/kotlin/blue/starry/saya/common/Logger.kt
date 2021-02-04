package blue.starry.saya.common

import ch.qos.logback.classic.Level
import mu.KLogger
import mu.KotlinLogging

internal fun KotlinLogging.createSayaLogger(name: String): KLogger {
    val logger = logger(name)
    val underlying = logger.underlyingLogger
    if (underlying is ch.qos.logback.classic.Logger) {
        underlying.level = Level.toLevel(Env.SAYA_LOG, Level.INFO)
    }

    return logger
}
