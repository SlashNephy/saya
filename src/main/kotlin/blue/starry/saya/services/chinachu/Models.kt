package blue.starry.saya.services.chinachu

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class Scheduler(override val json: JsonObject): JsonModel {
    val time by long
    val conflicts by modelList { Program(it) }
    val reserves by modelList { Program(it) }
}

data class Rule(override val json: JsonObject): JsonModel {
    val types by stringList
    val categories by stringList
    val channels by stringList
    val ignoreChannels by stringList
    val reserveFlags by stringList("reserve_flags")
    val ignoreFlags by stringList("ignore_flags")
    val hour by model { Hour(it) }
    val duration by nullableModel { Duration(it) }
    val reserveTitles by stringList("reserve_titles")
    val ignoreTitles by stringList("ignore_titles")
    val reserveDescriptions by stringList("reserve_descriptions")
    val ignoreDescriptions by stringList("ignore_descriptions")
    val recordedFormat by string
    val isDisabled by boolean { false }

    data class Hour(override val json: JsonObject): JsonModel {
        val start by int
        val end by int
    }

    data class Duration(override val json: JsonObject): JsonModel {
        val min by nullableInt
        val max by nullableInt
    }
}

open class Program(override val json: JsonObject): JsonModel {
    val id by string
    val category by string
    val title by string
    val fullTitle by string
    val detail by string
    val start by long
    val end by long
    val seconds by int
    val description by string
    val extra by jsonObject
    val channel by model { Channel(it) }
    val subTitle by string
    val episode by nullableInt
    val flags by stringList
}

open class Channel(override val json: JsonObject): JsonModel {
    val type by string
    val channel by string
    val name by string
    val id by string
    val sid by string
    val nid by string
    val hasLogoData by boolean
    val n by int
}

data class Schedule(override val json: JsonObject): JsonModel, Channel(json) {
    val programs by modelList { Program(it) }
}

data class Reserve(override val json: JsonObject): JsonModel, Program(json) {
    val isConflict by boolean
    val recordedFormat by string
}

data class Recording(override val json: JsonObject): JsonModel, Program(json) {
    val isManualReserved by boolean
    val priority by int
    val tuner by model { Tuner(it) }
    val command by string
    val pid by int
    val recorded by string
}

open class Recorded(override val json: JsonObject): JsonModel {
    val isConflict by boolean
    val recordedFormat by string
    val priority by int
    val tuner by model { Tuner(it) }
    val command by string
    val recorded by string
}

data class Tuner(override val json: JsonObject): JsonModel {
    val name by string
    val command by string
    val isScrambling by boolean
}

data class RecordedFile(override val json: JsonObject): JsonModel {
    val dev by int
    val ino by int
    val mode by int
    val uid by int
    val gid by int
    val rdev by int
    val size by int
    val blksize by int
    val blocks by int
    val atime by int
    val mtime by int
    val ctime by int
}

data class Status(override val json: JsonObject): JsonModel {
    val connectedCount by int
    val feature by model { Feature(it) }
    val system by model { System(it) }
    val operator by model { Process(it) }
    val wui by model { Process(it) }

    data class Feature(override val json: JsonObject): JsonModel {
        val previewer by boolean
        val streamer by boolean
        val filer by boolean
        val configurator by boolean
    }

    data class System(override val json: JsonObject): JsonModel {
        val core by int
    }

    data class Process(override val json: JsonObject): JsonModel {
        val alive by boolean
        val pid by int
    }
}

data class Storage(override val json: JsonObject): JsonModel {
    val recorded by long
    val size by long
    val used by long
    val avail by long
}
