package blue.starry.saya.services.gochan

/**
 * 5ch 関連のパースに失敗した際に投げる例外
 */
class GochanParseException(type: Type, content: Any): Exception("Failed to parse $type: \"$content\"") {
    enum class Type {
        Dat, Subject
    }
}
