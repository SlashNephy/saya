package blue.starry.saya.services.comments.nicolive.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class EmbeddedData(override val json: JsonObject): JsonModel {
    val program by model { Program(it) }
    val site by model { Site(it) }

    data class Program(override val json: JsonObject): JsonModel {
        val tag by model { Tag(it) }
        val status by string
        val beginTime by long
        val vposBaseTime by long

        data class Tag(override val json: JsonObject): JsonModel {
            val list by modelList { TagEach(it) }

            data class TagEach(override val json: JsonObject): JsonModel {
                val text by string
            }
        }
    }

    data class Site(override val json: JsonObject): JsonModel {
        val relive by model { Relive(it) }
        val tag by model { Tag(it) }

        data class Relive(override val json: JsonObject): JsonModel {
            val webSocketUrl by string
        }

        data class Tag(override val json: JsonObject): JsonModel {
            val revisionCheckIntervalMs by long
        }
    }
}

data class SearchPrograms(override val json: JsonObject): JsonModel {
    val meta by model { Meta(it) }
    val data by modelList { Data(it) }

    data class Meta(override val json: JsonObject) : JsonModel {
        val status by int
        val totalCount by int
        val ssId by string
    }

    data class Data(override val json: JsonObject): JsonModel {
        val id by string
        val title by string
        val description by string
        val thumbnailUrl by model { ThumbnailUrl(it) }
        val beginAt by string
        val endAt by string
        val showTime by model { ShowTime(it) }
        val onAirTime by model { ShowTime(it) }
        val liveCycle by string
        val providerType by string
        val providerId by string
        val socialGroupId by string
        val isMemberOnly by boolean
        val isPayProgram by boolean
        val isChannelRelatedOfficial by boolean
        val viewCount by int
        val commentCount by int
        val tags by modelList { Tag(it) }
        val deviceFilter by model { DeviceFilter(it) }
        val isPortrait by boolean
        val timeshift by model { Timeshift(it) }
        val contentOwner by model { ContentOwner(it) }

        data class ThumbnailUrl(override val json: JsonObject): JsonModel {
            val normal by string
        }

        data class ShowTime(override val json: JsonObject): JsonModel {
            val beginAt by string
            val endAt by string
        }

        data class Tag(override val json: JsonObject): JsonModel {
            val text by string
        }

        data class DeviceFilter(override val json: JsonObject): JsonModel {
            val isPlayable by boolean
            val isListing by boolean
            val isArchivePlayable by boolean
            val isChasePlayable by boolean
        }

        data class Timeshift(override val json: JsonObject): JsonModel {
            val enabled by boolean
            val status by string
        }

        data class ContentOwner(override val json: JsonObject): JsonModel {
            val type by string
            val id by string
            val icon by string
            val name by string
        }
    }
}

data class NicoliveWebSocketSystemJson(override val json: JsonObject): JsonModel {
    val type by string
    val data by model { Data(it) }

    data class Data(override val json: JsonObject): JsonModel {
        // seat
        val keepIntervalSec by long

        // room
        val messageServer by model { MessageServer(it) }
        val threadId by jsonElement

        // statistics
        val viewers by int
        val comments by int
        val adPoints by nullableInt
        val giftPoints by nullableInt

        data class MessageServer(override val json: JsonObject): JsonModel {
            val uri by string
        }
    }
}

data class NicoliveWebSocketMessageJson(override val json: JsonObject) : JsonModel {
    val chat by nullableModel { Chat(it) }
}

data class Chat(override val json: JsonObject) : JsonModel {
    val thread by string
    val no by int
    val vpos by int
    val date by long
    val dateUsec by int("date_usec")
    val mail by string { "" }
    val userId by string("user_id")
    val anonymity by int { 0 }
    val premium by int { 0 }
    val content by string
}
