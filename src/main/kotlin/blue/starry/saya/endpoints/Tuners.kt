package blue.starry.saya.endpoints

import blue.starry.saya.models.TunerProcess
import blue.starry.saya.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunApi
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getTuners() {
    get {
        MirakurunDataManager.Tuners.update()

        call.respond(
            MirakurunDataManager.Tuners.toList()
        )
    }
}

fun Route.getTuner() {
    get {
        val index: Int by call.parameters

        MirakurunDataManager.Tuners.update()

        call.respondOrNotFound(
            MirakurunDataManager.Tuners.find { it.index == index }
        )
    }
}

fun Route.getTunerProcess() {
    get {
        val index: Int by call.parameters

        MirakurunDataManager.Tuners.update()

        call.respond(
            TunerProcess(
                pid = MirakurunApi.getTunerProcess(index).pid
            )
        )
    }
}

fun Route.deleteTunerProcess() {
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
