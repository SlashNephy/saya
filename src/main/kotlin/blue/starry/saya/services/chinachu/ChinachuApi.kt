package blue.starry.saya.services.chinachu

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.encodeToString
import blue.starry.jsonkt.parseArray
import blue.starry.jsonkt.parseObject
import blue.starry.saya.common.Env
import blue.starry.saya.services.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*

object ChinachuApi {
    private val ApiBaseUri = "http://${Env.CHINACHU_HOST}:${Env.CHINACHU_PORT}/api"

    suspend fun getScheduler() = httpClient.get<String>("$ApiBaseUri/scheduler.json").parseObject {
        Scheduler(it)
    }

    suspend fun putScheduler() = httpClient.put<HttpResponse>("$ApiBaseUri/scheduler.json")

    suspend fun putSchedulerForce() = httpClient.put<HttpResponse>("$ApiBaseUri/scheduler/force.json")

    suspend fun getRules() = httpClient.get<String>("$ApiBaseUri/rules.json").parseArray {
        Rule(it)
    }

    suspend fun createRule(json: JsonObject) = httpClient.post<HttpResponse>("$ApiBaseUri/rules.json") {
        body = TextContent(json.encodeToString(), ContentType.Application.Json)
    }

    suspend fun getRule(ruleNum: Int) = httpClient.get<String>("$ApiBaseUri/rules/$ruleNum.json").parseObject {
        Rule(it)
    }

    suspend fun editRule(ruleNum: Int, json: JsonObject) = httpClient.put<HttpResponse>("$ApiBaseUri/rules/$ruleNum.json") {
        body = TextContent(json.encodeToString(), ContentType.Application.Json)
    }

    suspend fun deleteRule(ruleNum: Int) = httpClient.delete<HttpResponse>("$ApiBaseUri/rules/$ruleNum.json")

    suspend fun enableRule(ruleNum: Int) = httpClient.put<HttpResponse>("$ApiBaseUri/rules/$ruleNum/enable.json")

    suspend fun disableRule(ruleNum: Int) = httpClient.put<HttpResponse>("$ApiBaseUri/rules/$ruleNum/disable.json")

    suspend fun getProgram(programId: String) = httpClient.get<String>("$ApiBaseUri/program/$programId.json").parseObject {
        Program(it)
    }

    suspend fun putProgram(programId: String) = httpClient.put<HttpResponse>("$ApiBaseUri/program/$programId.json")

    suspend fun getSchedules() = httpClient.get<String>("$ApiBaseUri/schedule.json").parseArray {
        Schedule(it)
    }

    suspend fun getSchedule(channelId: String) = httpClient.get<String>("$ApiBaseUri/schedule/$channelId.json").parseObject {
        Schedule(it)
    }

    suspend fun getPrograms() = httpClient.get<String>("$ApiBaseUri/schedule/programs.json").parseArray {
        Program(it)
    }

    suspend fun getBroadcastingPrograms() = httpClient.get<String>("$ApiBaseUri/schedule/broadcasting.json").parseArray {
        Program(it)
    }

    suspend fun getChannelPrograms(channelId: String) = httpClient.get<String>("$ApiBaseUri/schedule/$channelId/programs.json").parseArray {
        Program(it)
    }

    suspend fun getChannelBroadcastingPrograms(channelId: String) = httpClient.get<String>("$ApiBaseUri/schedule/$channelId/programs.json").parseArray {
        Program(it)
    }

    suspend fun getReserves() = httpClient.get<String>("$ApiBaseUri/reserves.json").parseArray {
        Reserve(it)
    }

    suspend fun getReserve(programId: String) = httpClient.get<String>("$ApiBaseUri/reserves/$programId.json").parseObject {
        Reserve(it)
    }

    suspend fun deleteReserve(programId: String) = httpClient.delete<HttpResponse>("$ApiBaseUri/reserves/$programId.json")

    suspend fun getRecordings() = httpClient.get<String>("$ApiBaseUri/recording.json").parseArray {
        Recording(it)
    }

    suspend fun getRecording(programId: String) = httpClient.get<String>("$ApiBaseUri/recording/$programId.json").parseObject {
        Recording(it)
    }

    suspend fun deleteRecording(programId: String) = httpClient.delete<HttpResponse>("$ApiBaseUri/recording/$programId.json")

    private suspend fun getRecordingPreview(
        programId: String,
        format: String,
        width: Int?,
        height: Int?,
        size: String?
    ) = httpClient.get<ByteArray>("$ApiBaseUri/recording/$programId/preview.$format") {
        parameter("width", width)
        parameter("height", height)
        parameter("size", size)
    }

    suspend fun getRecordingPngPreview(
        programId: String,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordingPreview(programId, "png", width, height, size)

    suspend fun getRecordingJpgPreview(
        programId: String,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordingPreview(programId, "jpg", width, height, size)

    suspend fun getRecordingBase64Preview(
        programId: String,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordingPreview(programId, "txt", width, height, size)

    suspend fun getRecordingXSPFWatch(
        programId: String,
        ext: String? = null,
        prefix: String? = null
    ) = httpClient.get<String>("$ApiBaseUri/recording/$programId/watch.xspf") {
        parameter("ext", ext)
        parameter("prefix", prefix)
    }

    suspend fun getRecordingM2TSWatch(
        programId: String,
        ss: Int? = null,
        t: Int? = null,
        f: String? = null,
        cv: String? = null,
        ca: String? = null,
        bv: String? = null,
        ba: String? = null,
        s: String? = null,
        r: Int? = null
    ) = httpClient.get<HttpStatement>("$ApiBaseUri/recording/$programId/watch.m2ts") {
        parameter("ss", ss)
        parameter("t", t)
        parameter("f", f)
        parameter("c:v", cv)
        parameter("c:a", ca)
        parameter("b:v", bv)
        parameter("b:a", ba)
        parameter("s", s)
        parameter("r", r)
    }

    suspend fun getRecorded() = httpClient.get<String>("$ApiBaseUri/recorded.json").parseArray {
        Recorded(it)
    }

    suspend fun cleanRecorded() = httpClient.put<HttpResponse>("$ApiBaseUri/recorded.json")

    suspend fun getRecorded(programId: String) = httpClient.get<String>("$ApiBaseUri/recorded/$programId.json").parseObject {
        Recorded(it)
    }

    suspend fun deleteRecorded(programId: String) = httpClient.delete<HttpResponse>("$ApiBaseUri/recorded/$programId.json")

    suspend fun getRecordedFileInfo(programId: String) = httpClient.get<String>("$ApiBaseUri/recorded/$programId/file.json").parseObject {
        RecordedFile(it)
    }

    suspend fun getRecordedFile(programId: String) = httpClient.get<HttpStatement>("$ApiBaseUri/recorded/$programId/file.m2ts")

    suspend fun deleteRecordedFile(programId: String) = httpClient.delete<HttpResponse>("$ApiBaseUri/recorded/$programId/file.m2ts")

    private suspend fun getRecordedPreview(
        programId: String,
        format: String,
        pos: Int?,
        width: Int?,
        height: Int?,
        size: String?
    ) = httpClient.get<ByteArray>("$ApiBaseUri/recorded/$programId/preview.$format") {
        parameter("pos", pos)
        parameter("width", width)
        parameter("height", height)
        parameter("size", size)
    }

    suspend fun getRecordedPngPreview(
        programId: String,
        pos: Int? = null,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordedPreview(programId, "png", pos, width, height, size)

    suspend fun getRecordedJpgPreview(
        programId: String,
        pos: Int? = null,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordedPreview(programId, "jpg", pos, width, height, size)

    suspend fun getRecordedBase64Preview(
        programId: String,
        pos: Int? = null,
        width: Int? = null,
        height: Int? = null,
        size: String? = null
    ) = getRecordedPreview(programId, "txt", pos, width, height, size)

        suspend fun getRecordedXSPFWatch(
        programId: String,
        ext: String? = null,
        prefix: String? = null
    ) = httpClient.get<String>("$ApiBaseUri/recorded/$programId/watch.xspf") {
        parameter("ext", ext)
        parameter("prefix", prefix)
    }

    private suspend fun getRecordedWatch(
        programId: String,
        format: String,
        ss: Int? = null,
        t: Int? = null,
        f: String? = null,
        cv: String? = null,
        ca: String? = null,
        bv: String? = null,
        ba: String? = null,
        s: String? = null,
        r: Int? = null
    ) = httpClient.get<HttpStatement>("$ApiBaseUri/recorded/$programId/watch.$format") {
        parameter("ss", ss)
        parameter("t", t)
        parameter("f", f)
        parameter("c:v", cv)
        parameter("c:a", ca)
        parameter("b:v", bv)
        parameter("b:a", ba)
        parameter("s", s)
        parameter("r", r)
    }

    suspend fun getRecordedM2TSWatch(
        programId: String,
        ss: Int? = null,
        t: Int? = null,
        f: String? = null,
        cv: String? = null,
        ca: String? = null,
        bv: String? = null,
        ba: String? = null,
        s: String? = null,
        r: Int? = null
    ) = getRecordedWatch(programId, "m2ts", ss, t, f, cv, ca, bv, ba, s, r)

    suspend fun getRecordedMP4Watch(
        programId: String,
        ss: Int? = null,
        t: Int? = null,
        f: String? = null,
        cv: String? = null,
        ca: String? = null,
        bv: String? = null,
        ba: String? = null,
        s: String? = null,
        r: Int? = null
    ) = getRecordedWatch(programId, "mp4", ss, t, f, cv, ca, bv, ba, s, r)

    suspend fun getChannelLogo(channelId: String) = httpClient.get<ByteArray>("$ApiBaseUri/channel/$channelId/logo.png")

    suspend fun getChannelXSPFWatch(
        channelId: String,
        ext: String? = null,
        prefix: String? = null
    ) = httpClient.get<String>("$ApiBaseUri/channel/$channelId/watch.xspf") {
        parameter("ext", ext)
        parameter("prefix", prefix)
    }

    suspend fun getChannelM2TSWatch(
        channelId: String,
        ss: Int? = null,
        t: Int? = null,
        f: String? = null,
        cv: String? = null,
        ca: String? = null,
        bv: String? = null,
        ba: String? = null,
        s: String? = null,
        r: Int? = null
    ) = httpClient.get<HttpStatement>("$ApiBaseUri/channel/$channelId/watch.m2ts") {
        parameter("ss", ss)
        parameter("t", t)
        parameter("f", f)
        parameter("c:v", cv)
        parameter("c:a", ca)
        parameter("b:v", bv)
        parameter("b:a", ba)
        parameter("s", s)
        parameter("r", r)
    }

    private suspend fun getLog(format: String) = httpClient.get<String>("$ApiBaseUri/log/$format.txt")

    suspend fun getWUILog() = getLog("wui")

    suspend fun getOperatorLog() = getLog("operator")

    suspend fun getSchedulerLog() = getLog("scheduler")

    private suspend fun getLogStream(format: String) = httpClient.get<HttpStatement>("$ApiBaseUri/log/$format/stream.txt")

    suspend fun getWUILogStream() = getLogStream("wui")

    suspend fun getOperatorLogStream() = getLogStream("operator")

    suspend fun getSchedulerLogStream() = getLogStream("scheduler")

    suspend fun getStatus() = httpClient.get<String>("$ApiBaseUri/status.json").parseObject {
        Status(it)
    }

    suspend fun getStorage() = httpClient.get<String>("$ApiBaseUri/storage.json").parseObject {
        Storage(it)
    }
}
