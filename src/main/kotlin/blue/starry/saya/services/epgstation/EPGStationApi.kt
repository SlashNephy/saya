package blue.starry.saya.services.epgstation

import blue.starry.jsonkt.parseArray
import blue.starry.saya.common.Env
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*

object EPGStationApi {
    val ApiBaseUri = "http://${Env.EPGSTATION_HOST}:${Env.EPGSTATION_PORT}/api"

    suspend fun getChannels() = SayaHttpClient.get<String>("/channels").parseArray {
        Channel(it)
    }

    suspend fun getChannelLogo(id: Int) = SayaHttpClient.get<ByteArray>("/channels/$id/logo")
}
