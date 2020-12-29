package blue.starry.saya.endpoints

import blue.starry.saya.common.respondOrNotFound
import blue.starry.saya.services.mirakurun.MirakurunDataManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.getGenres() {
    get {
        call.respond(
            MirakurunDataManager.Genres.toList()
        )
    }
}

fun Route.getGenre() {
    get {
        val id: Int by call.parameters

        call.respondOrNotFound(
            MirakurunDataManager.Genres.find { it.id == id }
        )
    }
}

fun Route.getGenrePrograms() {
    get {
        val id: Int by call.parameters
        val genre = MirakurunDataManager.Genres.find { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respond(
            MirakurunDataManager.Programs.filter { it.genres.contains(genre.id) }
        )
    }
}
