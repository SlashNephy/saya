package blue.starry.saya.services.chinachu

import blue.starry.saya.common.ReadOnlyContainer

object ChinachuDataManager {
    val Recorded = ReadOnlyContainer {
        ChinachuApi.getRecorded().mapNotNull { chinachu ->
            chinachu.toSayaRecordedProgram()
        }.reversed()
    }

    val Reserves = ReadOnlyContainer {
        ChinachuApi.getReserves().mapNotNull { chinachu ->
            chinachu.toSayaReservedProgram()
        }.reversed()
    }

    val Recordings = ReadOnlyContainer {
        ChinachuApi.getRecordings().mapNotNull { chinachu ->
            chinachu.toSayaRecordingProgram()
        }.reversed()
    }

    val Rules = ReadOnlyContainer {
        ChinachuApi.getRules().mapIndexed { index, chinachu ->
            chinachu.toSayaRule(index)
        }
    }
}
