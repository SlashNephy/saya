package blue.starry.saya.common

operator fun <A, B> Pair<A, B>?.component1(): A? {
    return this?.first
}

operator fun <A, B> Pair<A, B>?.component2(): B? {
    return this?.second
}
