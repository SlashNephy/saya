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
