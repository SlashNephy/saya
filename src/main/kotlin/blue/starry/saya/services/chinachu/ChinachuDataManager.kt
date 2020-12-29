package blue.starry.saya.services.chinachu

import blue.starry.saya.common.ReadOnlyContainer
import mu.KotlinLogging

object ChinachuDataManager {
    private val logger = KotlinLogging.logger("saya.chinachu")

    val Recorded = ReadOnlyContainer {
        ChinachuApi.getRecorded().map { chinachu ->
            chinachu.toSayaRecordedProgram()
        }.reversed()
    }

    val Reserves = ReadOnlyContainer {
        ChinachuApi.getReserves().map { chinachu ->
            chinachu.toSayaReservedProgram()
        }.reversed()
    }

    val Recordings = ReadOnlyContainer {
        ChinachuApi.getRecordings().map { chinachu ->
            chinachu.toSayaRecordingProgram()
        }.reversed()
    }

    val Rules = ReadOnlyContainer {
        ChinachuApi.getRules().mapIndexed { index, chinachu ->
            chinachu.toSayaRule(index)
        }
    }
}
