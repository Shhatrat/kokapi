package pl.kokapi.endpoints

import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.GeocacheLogDto
import pl.kokapi.http.dto.SubmitLogResponseDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogType

class LogsClient internal constructor(private val engine: HttpEngine) {

    suspend fun get(logUuid: String): GeocacheLog {
        val body = engine.get(
            "logs/entry", mapOf(
                "log_uuid" to logUuid,
                "fields" to "uuid|cache_code|user|type|date|comment",
            )
        )
        return json.decodeFromString<GeocacheLogDto>(body).toDomain()
    }

    suspend fun submit(
        cacheCode: String,
        type: LogType,
        comment: String = "",
        date: String? = null,
        password: String? = null,
    ): String {
        val params = buildMap {
            put("cache_code", cacheCode)
            put("logtype", type.okapiName)
            put("comment", comment)
            date?.let { put("when", it) }
            password?.let { put("password", it) }
        }
        val body = engine.post("logs/submit", params)
        val response = json.decodeFromString<SubmitLogResponseDto>(body)
        if (!response.success) throw KokapiException.ApiError("submit_failed", response.message)
        return response.logUuid
            ?: throw KokapiException.ApiError("no_uuid", "Log submitted but no UUID returned")
    }

    suspend fun delete(logUuid: String) {
        engine.post("logs/delete", mapOf("log_uuid" to logUuid))
    }
}
