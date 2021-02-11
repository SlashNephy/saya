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

fun <T: Any> Mutex.bindTo(obj: T) = object: ScopedMutex<T> {
    override suspend fun <R> withLock(block: suspend (safeObj: T) -> R): R {
        return this@bindTo.withLock {
            block(obj)
        }
    }
}

interface ScopedMutex<T: Any> {
    suspend fun <R> withLock(block: suspend (T) -> R): R
}
