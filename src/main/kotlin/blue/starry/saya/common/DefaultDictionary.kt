package blue.starry.saya.common

class DefaultDictionary<K, V>(private val map: MutableMap<K, V>, private val default: () -> V): MutableMap<K, V> by map {
    constructor(default: () -> V): this(mutableMapOf(), default)

    override fun get(key: K): V {
        return map.getOrPut(key, default)
    }
}
