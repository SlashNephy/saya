package blue.starry.saya.services.epgstation

import blue.starry.jsonkt.parseArray
import blue.starry.saya.Env
import blue.starry.saya.services.epgstation.models.Channel
import blue.starry.saya.services.httpClient
import io.ktor.client.request.*

object EPGStationApi {
    val ApiBaseUri = "http://${Env.EPGSTATION_HOST}:${Env.EPGSTATION_PORT}/api"

    suspend fun getChannels() = httpClient.get<String>("/channels").parseArray {
        Channel(it)
    }

    suspend fun getChannelLogo(id: Int) = httpClient.get<ByteArray>("/channels/$id/logo")
}
