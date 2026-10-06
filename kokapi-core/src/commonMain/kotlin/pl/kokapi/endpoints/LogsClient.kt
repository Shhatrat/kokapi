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
import pl.kokapi.model.enum.LogType

/** Client for reading, submitting, and managing geocache log entries and their photos. */
class LogsClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "uuid|cache_code|user|type|date|comment|images|was_recommended|needs_maintenance2|date_created|last_modified|location|oc_team_entry"

    /** Fetches a single log entry by its UUID. */
    suspend fun get(logUuid: String): GeocacheLog {
        val body = engine.get(
            "logs/entry", mapOf(
                "log_uuid" to logUuid,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<GeocacheLogDto>(body).toDomain()
    }

    /** Fetches multiple log entries by their UUIDs, returned as a UUID-keyed map. */
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

    /** Fetches the most recent logs for a cache. */
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

    /**
     * Fetches logs submitted by a specific user.
     *
     * Requires authentication when fetching logs for the currently authenticated user
     * to include private entries.
     */
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

    /**
     * Submits a new log entry for a geocache.
     *
     * @param type Log type (Found, DNF, Comment, Attended, etc.).
     * @param date ISO-8601 date string (e.g. "2024-06-01"); defaults to today on the server.
     * @param password Cache password, required if the cache is password-protected.
     * @param commentFormat Format of the comment: "plaintext" or "html".
     * @param recommend Whether to recommend this cache (costs one recommendation point).
     * @param rating Optional numeric rating (1–5).
     * @param needsMaintenance Set to true to flag the cache as needing maintenance.
     * @return UUID of the newly created log entry.
     * @throws KokapiException.ApiError if the server rejects the submission.
     * Requires authentication.
     */
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

    /**
     * Deletes a log entry.
     *
     * Requires authentication; the caller must own the log or be an admin.
     */
    suspend fun delete(logUuid: String) {
        engine.post("logs/delete", mapOf("log_uuid" to logUuid))
    }

    /**
     * Edits an existing log entry.
     *
     * Only fields that are provided (non-null) will be updated.
     * Requires authentication; the caller must own the log.
     */
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

    /**
     * Returns the available log types and constraints for a cache or the site.
     *
     * @param cacheCode Optional cache code; if omitted, returns site-wide defaults.
     */
    suspend fun capabilities(cacheCode: String? = null): LogCapabilities {
        val params = buildMap {
            cacheCode?.let { put("cache_code", it) }
        }
        val body = engine.get("logs/capabilities", params)
        return json.decodeFromString<LogCapabilitiesDto>(body).toDomain()
    }

    /**
     * Attaches a photo to an existing log entry.
     *
     * @param imageBase64 Base64-encoded image data.
     * @param isSpoiler Whether the image reveals the cache location.
     * @return UUID of the newly created image.
     * @throws KokapiException.ApiError if the server rejects the image.
     * Requires authentication.
     */
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

    /**
     * Updates the caption or spoiler flag of a log photo.
     *
     * Only non-null fields are changed.
     * Requires authentication.
     */
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

    /**
     * Deletes a log photo.
     *
     * Requires authentication; the caller must own the log.
     */
    suspend fun deleteImage(imageUuid: String) {
        engine.post("logs/images/delete", mapOf("image_uuid" to imageUuid))
    }
}
