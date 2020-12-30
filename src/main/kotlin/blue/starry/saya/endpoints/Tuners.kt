package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import blue.starry.saya.services.mirakurun.toSayaTunerProcess
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getTuners() {
    get {
        call.respond(
            MirakurunDataManager.Tuners.toList()
        )
    }
}

fun Route.getTunerByIndex() {
    get {
        val index: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Tuners.find { it.index == index }
        )
    }
}

fun Route.getTunerProcessByIndex() {
    get {
        val index: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Tuners.find { it.index == index }?.toSayaTunerProcess()
        )
    }
}

fun Route.deleteTunerProcessByIndex() {
    delete {
        val index: Int by call.parameters

        MirakurunApi.deleteTunerProcess(index)
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.putTuners() {
    put {
        MirakurunDataManager.Tuners.update()
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.getMirakurunTunerByIndex() {
    get {
        val index: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Tuners.find { it.index == index }?.json
        )
    }
}

fun Route.getMirakurunTunerProcessByIndex() {
    get {
        val index: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Tuners.find { it.index == index }?.toSayaTunerProcess()?.json
        )
    }
}

fun Route.getMirakurunTuners() {
    get {
        call.respondOrNotFound(
            MirakurunDataManager.Tuners.toList().map { it.json }
        )
    }
}
