package blue.starry.saya.services.mirakurun

import blue.starry.jsonkt.parseArray
import blue.starry.jsonkt.parseObject
import blue.starry.saya.services.SayaHttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*

class MirakurunApi(host: String, port: Int) {
    private val apiBaseUrl = "http://$host:$port/api"

    suspend fun putVersionUpdate(force: Boolean? = null) = SayaHttpClient.put<HttpResponse>("$apiBaseUrl/version/update") {
        parameter("force", force)
    }

    suspend fun getVersion() = SayaHttpClient.get<String>("$apiBaseUrl/version").parseObject {
        Version(it)
    }

    suspend fun getTuners() = SayaHttpClient.get<String>("$apiBaseUrl/tuners").parseArray {
        Tuner(it)
    }

    suspend fun getTunerProcess(index: Int) = SayaHttpClient.get<String>("$apiBaseUrl/tuners/$index/process").parseObject {
        TunerProcess(it)
    }

    suspend fun deleteTunerProcess(index: Int) = SayaHttpClient.delete<String>("$apiBaseUrl/tuners/$index/process").parseObject {
        TunerProcess(it)
    }

    suspend fun getTuner(index: Int) = SayaHttpClient.get<String>("$apiBaseUrl/tuners/$index").parseObject {
        Tuner(it)
    }

    suspend fun getStatus() = SayaHttpClient.get<String>("$apiBaseUrl/status").parseObject {
        Status(it)
    }

    suspend fun getServices(
        serviceId: Int? = null,
        networkId: Int? = null,
        name: String? = null,
        type: Int? = null,
        channelType: String? = null,
        channelChannel: String? = null
    ) = SayaHttpClient.get<String>("$apiBaseUrl/services") {
        parameter("serviceId", serviceId)
        parameter("networkId", networkId)
        parameter("name", name)
        parameter("type", type)
        parameter("channel.type", channelType)
        parameter("channel.channel", channelChannel)
    }.parseArray {
        Service(it)
    }

    suspend fun getServiceStream(id: Long, priority: Int? = null, decode: Boolean? = null) = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/services/$id/stream") {
        header("X-Mirakurun-Priority", priority)
        parameter("decode", when (decode) {
            true -> 1
            false -> 0
            else -> decode
        })
    }

    suspend fun getServiceLogo(id: Long) = SayaHttpClient.get<ByteArray>("$apiBaseUrl/services/$id/logo")

    suspend fun getService(id: Long) = SayaHttpClient.get<String>("$apiBaseUrl/services/$id").parseObject {
        Service(it)
    }

    suspend fun getChannelStream(type: String, channel: String, id: Int, priority: Int? = null, decode: Boolean? = null) = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/channels/$type/$channel/services/$id/stream") {
        header("X-Mirakurun-Priority", priority)
        parameter("decode", when (decode) {
            true -> 1
            false -> 0
            else -> decode
        })
    }

    suspend fun getChannelService(type: String, channel: String, id: Int) = SayaHttpClient.get<String>("$apiBaseUrl/channels/$type/$channel/services/$id").parseObject {
        Service(it)
    }

    suspend fun getChannelServices(type: String, channel: String) = SayaHttpClient.get<String>("$apiBaseUrl/channels/$type/$channel/services").parseArray {
        Service(it)
    }

    suspend fun putRestart() = SayaHttpClient.put<HttpResponse>("$apiBaseUrl/restart")

    suspend fun getPrograms(networkId: Int? = null, serviceId: Int? = null, eventId: Int? = null) = SayaHttpClient.get<String>("$apiBaseUrl/programs") {
        parameter("networkId", networkId)
        parameter("serviceId", serviceId)
        parameter("eventId", eventId)
    }.parseArray {
        Program(it)
    }

    suspend fun getProgramStream(id: Long, priority: Int? = null, decode: Boolean? = null) = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/programs/$id/stream") {
        header("X-Mirakurun-Priority", priority)
        parameter("decode", when (decode) {
            true -> 1
            false -> 0
            else -> decode
        })
    }

    suspend fun getPrograms(id: Long) = SayaHttpClient.get<String>("$apiBaseUrl/programs/$id").parseObject {
        Program(it)
    }

    suspend fun getLogStream() = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/log/stream")

    suspend fun getLog() = SayaHttpClient.get<String>("$apiBaseUrl/log")

    suspend fun getEventStream(resource: String? = null, type: String? = null) = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/events/stream") {
        parameter("resource", resource)
        parameter("type", type)
    }

    suspend fun getEvents() = SayaHttpClient.get<String>("$apiBaseUrl/events").parseArray {
        Event(it)
    }

    suspend fun getChannels(type: String? = null, channel: String? = null, name: String? = null) = SayaHttpClient.get<String>("$apiBaseUrl/channels") {
        parameter("type", type)
        parameter("channel", channel)
        parameter("name", name)
    }.parseArray {
        Service.Channel(it)
    }

    suspend fun getChannelStream(type: String, channel: String, priority: Int? = null, decode: Boolean? = null) = SayaHttpClient.get<HttpStatement>("$apiBaseUrl/channels/$type/$channel/stream") {
        header("X-Mirakurun-Priority", priority)
        parameter("decode", when (decode) {
            true -> 1
            false -> 0
            else -> decode
        })
    }

    suspend fun getChannel(type: String, channel: String) = SayaHttpClient.get<String>("$apiBaseUrl/channels/$type/$channel").parseObject {
        Service.Channel(it)
    }
}
