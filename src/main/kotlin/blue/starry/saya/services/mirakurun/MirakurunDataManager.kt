package blue.starry.saya.services.mirakurun

import blue.starry.saya.common.ReadOnlyContainer
import blue.starry.saya.models.Genre
import blue.starry.saya.models.Logo
import org.apache.commons.codec.binary.Base64

object MirakurunDataManager {
    val Services = ReadOnlyContainer {
        MirakurunApi.getServices().map { mirakurun ->
            mirakurun.toSayaService()
        }
    }

    val Channels = ReadOnlyContainer {
        MirakurunApi.getChannels().mapNotNull { mirakurun ->
            mirakurun.toSayaChannel()
        }
    }

    val Programs = ReadOnlyContainer {
        MirakurunApi.getPrograms().mapNotNull { mirakurun ->
            mirakurun.toSayaProgram()
        }
    }

    val Tuners = ReadOnlyContainer {
        MirakurunApi.getTuners().map { mirakurun ->
            mirakurun.toSayaTuner()
        }
    }

    val Logos = ReadOnlyContainer {
        Services.filter {
            it.logoId != null
        }.distinctBy {
            it.logoId
        }.map { service ->
            Logo(
                id = service.logoId!!,
                service = service,
                data = Base64.encodeBase64String(MirakurunApi.getServiceLogo(service.id))
            )
        }.sortedBy {
            it.id
        }
    }

    val Genres = ReadOnlyContainer {
        mainGenres.flatMap { (lv1, main) ->
            subGenres[lv1]!!.filterValues { it != null }.map { (lv2, sub) ->
                val id = lv1 * 16 + lv2

                Genre(
                    id = id,
                    main = main,
                    sub = sub!!,
                    count = Programs.filter { program ->
                        program.genres.contains(id)
                    }.count()
                )
            }
        }
    }
}
