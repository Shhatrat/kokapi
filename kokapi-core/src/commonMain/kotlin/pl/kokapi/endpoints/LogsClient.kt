package pl.kokapi.endpoints

import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.AddImageResponseDto
import pl.kokapi.http.dto.GeocacheLogDto
import pl.kokapi.http.dto.LogCapabilitiesDto
import pl.kokapi.http.dto.SubmitLogResponseDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogCapabilities
import pl.kokapi.model.LogType

class LogsClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "uuid|cache_code|user|type|date|comment"

    suspend fun get(logUuid: String): GeocacheLog {
        val body = engine.get(
            "logs/entry", mapOf(
                "log_uuid" to logUuid,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<GeocacheLogDto>(body).toDomain()
    }

    suspend fun getEntries(logUuids: List<String>): Map<String, GeocacheLog> {
        val body = engine.get(
            "logs/entries", mapOf(
                "log_uuids" to logUuids.joinToString("|"),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<Map<String, GeocacheLogDto>>(body)
            .mapValues { it.value.toDomain() }
    }

    suspend fun getForCache(
        cacheCode: String,
        limit: Int = 20,
        offset: Int = 0,
    ): List<GeocacheLog> {
        val body = engine.get(
            "logs/logs", mapOf(
                "cache_code" to cacheCode,
                "limit" to limit.toString(),
                "offset" to offset.toString(),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<List<GeocacheLogDto>>(body).map { it.toDomain() }
    }

    suspend fun getUserLogs(
        userUuid: String,
        limit: Int = 20,
        offset: Int = 0,
    ): List<GeocacheLog> {
        val body = engine.get(
            "logs/userlogs", mapOf(
                "user_uuid" to userUuid,
                "limit" to limit.toString(),
                "offset" to offset.toString(),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<List<GeocacheLogDto>>(body).map { it.toDomain() }
    }

    suspend fun submit(
        cacheCode: String,
        type: LogType,
        comment: String = "",
        date: String? = null,
        password: String? = null,
        commentFormat: String = "plaintext",
        recommend: Boolean = false,
        rating: Int? = null,
        needsMaintenance: Boolean? = null,
    ): String {
        val params = buildMap {
            put("cache_code", cacheCode)
            put("logtype", type.okapiName)
            put("comment", comment)
            put("comment_format", commentFormat)
            put("recommend", recommend.toString())
            date?.let { put("when", it) }
            password?.let { put("password", it) }
            rating?.let { put("rating", it.toString()) }
            needsMaintenance?.let { put("needs_maintenance2", it.toString()) }
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

    suspend fun edit(
        logUuid: String,
        comment: String? = null,
        commentFormat: String = "plaintext",
        date: String? = null,
        recommend: Boolean? = null,
    ) {
        val params = buildMap {
            put("log_uuid", logUuid)
            comment?.let {
                put("comment", it)
                put("comment_format", commentFormat)
            }
            date?.let { put("when", it) }
            recommend?.let { put("recommend", it.toString()) }
        }
        engine.post("logs/edit", params)
    }

    suspend fun capabilities(cacheCode: String? = null): LogCapabilities {
        val params = buildMap {
            cacheCode?.let { put("cache_code", it) }
        }
        val body = engine.get("logs/capabilities", params)
        return json.decodeFromString<LogCapabilitiesDto>(body).toDomain()
    }

    suspend fun addImage(
        logUuid: String,
        imageBase64: String,
        caption: String? = null,
        isSpoiler: Boolean = false,
    ): String {
        val params = buildMap {
            put("log_uuid", logUuid)
            put("image", imageBase64)
            put("is_spoiler", isSpoiler.toString())
            caption?.let { put("caption", it) }
        }
        val body = engine.post("logs/images/add", params)
        val response = json.decodeFromString<AddImageResponseDto>(body)
        if (!response.success) throw KokapiException.ApiError("add_image_failed", response.message)
        return response.imageUuid
            ?: throw KokapiException.ApiError("no_image_uuid", "Image added but no UUID returned")
    }

    suspend fun editImage(
        imageUuid: String,
        caption: String? = null,
        isSpoiler: Boolean? = null,
    ) {
        val params = buildMap {
            put("image_uuid", imageUuid)
            caption?.let { put("caption", it) }
            isSpoiler?.let { put("is_spoiler", it.toString()) }
        }
        engine.post("logs/images/edit", params)
    }

    suspend fun deleteImage(imageUuid: String) {
        engine.post("logs/images/delete", mapOf("image_uuid" to imageUuid))
    }
}
