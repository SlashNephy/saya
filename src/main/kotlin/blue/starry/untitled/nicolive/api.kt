package blue.starry.untitled.nicolive

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*
import blue.starry.jsonkt.parseObject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun searchLivePrograms(word: String): SearchPrograms {
    return HttpClient().use { client ->
        client.get<String>("https://api.cas.nicovideo.jp/v2/search/programs.json?liveStatus=onair&offset=0&sort=startTime&limit=20&searchWord=$word&searchTargets=keyword&order=desc") {
            header("X-Frontend-Id", "89")
            userAgent("nicocas-iOS/5.16.0 nico-webview/1.0.0")
        }.parseObject {
            SearchPrograms(it)
        }
    }
}

suspend fun main() {
    searchLivePrograms("ニコニコ実況").data.forEach {
        println(it.id)
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
