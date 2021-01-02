package blue.starry.saya.services.mirakurun

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class Version(override val json: JsonObject): JsonModel {
    val current by string
    val latest by string
}

data class Error(override val json: JsonObject): JsonModel {
    val code by int
    val reason by nullableString
    val errors by modelList { ErrorMessage(it) }

    data class ErrorMessage(override val json: JsonObject): JsonModel {
        val errorCode by string
        val message by string
        val location by string
    }
}

data class Tuner(override val json: JsonObject): JsonModel {
    val index by int
    val name by string
    val types by stringList
    val command by nullableString
    val pid by nullableInt
    val users by modelList { User(it) }
    val isAvailable by boolean
    val isRemote by boolean
    val isFree by boolean
    val isUsing by boolean
    val isFault by boolean

    data class User(override val json: JsonObject): JsonModel {
        val id by string
        val priority by int
        val agent by nullableString
        val url by string
        val disableDecoder by boolean { false }
        val streamSetting by nullableModel { StreamSetting(it) }
        val streamInfo by nullableJsonObject

        data class StreamSetting(override val json: JsonObject): JsonModel {
            val channel by model { Service.Channel(it) }
            val networkId by nullableInt
            val serviceId by nullableInt
            val eventId by nullableInt
            val noProvide by boolean { false }
            val parseNIT by boolean { false }
            val parseSDT by boolean { false }
            val parseEIT by boolean { false }
        }
    }
}

data class TunerProcess(override val json: JsonObject): JsonModel {
    val pid by int
}

data class Status(override val json: JsonObject): JsonModel {
    val time by long
    val version by string
    val process by model { Process(it) }

    data class Process(override val json: JsonObject): JsonModel {
        val arch by string
        val platform by string
        val versions by model { Versions(it) }
        val env by jsonObject
        val pid by int
        val memoryUsage by model { MemoryUsage(it) }

        data class Versions(override val json: JsonObject): JsonModel {
            val node by string
            val v8 by string
            val uv by string
            val zlib by string
            val brotli by string
            val ares by string
            val modules by string
            val nghttp2 by string
            val napi by string
            val llhttp by string
            val openssl by string
            val cldr by string
            val icu by string
            val tz by string
            val unicode by string
        }

        data class MemoryUsage(override val json: JsonObject): JsonModel {
            val rss by long
            val heapTotal by long
            val heapUsed by long
            val external by long
            val arrayBuffers by long
        }
    }
}

data class Service(override val json: JsonObject): JsonModel {
    val id by long
    val serviceId by int
    val networkId by int
    val name by string
    val type by int
    val logoId by int
    val remoteControlKeyId by nullableInt
    val channel by model { Channel(it) }
    val hasLogoData by boolean

    data class Channel(override val json: JsonObject): JsonModel {
        val type by string
        val channel by string
        val name by nullableString
        val satelite by nullableString
        val space by nullableInt
        val services by modelList { Service(it) }
    }
}

data class Program(override val json: JsonObject): JsonModel {
    val id by long
    val eventId by int
    val serviceId by int
    val networkId by int
    val startAt by long
    val duration by int
    val isFree by boolean
    val name by nullableString
    val description by nullableString
    val video by nullableModel { Video(it) }
    val audio by nullableModel { Audio(it) }
    val genres by modelList { Genre(it) }
    val extended by nullableJsonObject
    val relatedItems by modelList { RelatedItem(it) }
    val series by nullableModel { Series(it) }

    data class Video(override val json: JsonObject): JsonModel {
        val type by string
        val resolution by string
        val streamContent by int
        val componentType by int
    }

    data class Audio(override val json: JsonObject): JsonModel {
        val samplingRate by int
        val componentType by int
    }

    data class Genre(override val json: JsonObject): JsonModel {
        val lv1 by int
        val lv2 by int
        val un1 by int
        val un2 by int
    }

    data class RelatedItem(override val json: JsonObject): JsonModel {
        val networkId by long
        val serviceId by long
        val eventId by long
    }

    data class Series(override val json: JsonObject): JsonModel {
        val id by nullableInt
        val repeat by nullableInt
        val pattern by nullableInt
        val expiresAt by nullableInt
        val lastEpisode by nullableInt
        val name by nullableString
    }
}

data class Event(override val json: JsonObject): JsonModel {
    val resource by enum<String, Resource>()
    val type by enum<String, Type>()
    val data by jsonObject
    val time by long

    enum class Resource: StringJsonEnum {
        Program, Service, Tuner;

        override val value
            get() = name.decapitalize()
    }

    enum class Type: StringJsonEnum {
        Create, Update, Redefine;

        override val value
            get() = name.decapitalize()
    }
}
