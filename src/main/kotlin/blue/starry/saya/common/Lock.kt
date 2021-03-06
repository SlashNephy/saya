package blue.starry.saya.common

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <T: Any> T.asThreadSafe() = object: ScopedMutex<T> {
    val mutex = Mutex()

    override suspend fun <R> withLock(block: suspend (safeObj: T) -> R): R {
        return mutex.withLock {
            block(this@asThreadSafe)
        }
    }
}

interface ScopedMutex<T: Any> {
    suspend fun <R> withLock(block: suspend (T) -> R): R
}
