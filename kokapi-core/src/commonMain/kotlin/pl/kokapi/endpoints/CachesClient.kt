package pl.kokapi.endpoints

import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.GeocacheDto
import pl.kokapi.http.dto.SuccessDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Geocache

/** Client for retrieving and managing specific geocaches. */
class CachesClient internal constructor(private val engine: HttpEngine) {

    private val publicFields = listOf(
        // identity & discovery
        "code", "name", "location", "type", "status", "url", "gc_code",
        // difficulty & metadata
        "difficulty", "terrain", "owner",
        "date_created", "date_hidden", "last_modified", "last_found",
        // activity
        "founds", "notfounds", "willattends", "watchers",
        // ratings
        "rating", "rating_votes", "recommendations",
        // content
        "description", "short_description", "hint2",
        "names", "descriptions", "short_descriptions", "hints2",
        // media & extras
        "images", "trackables_count", "trackables", "alt_wpts",
        // location details
        "country2", "region",
        // logistics
        "size2", "req_passwd", "trip_time", "trip_distance", "oxsize", "internal_id",
        // status & flags
        "needs_maintenance",
        // attributes & annotations
        "attr_acodes", "latest_logs", "oc_team_annotation", "attribution_note", "protection_areas",
    ).joinToString("|")

    private val authFields = listOf(
        "is_found", "is_not_found", "is_watched", "is_ignored",
        "is_recommended", "my_rating", "my_notes",
    ).joinToString("|")

    internal val defaultFields get() =
        if (engine.isAuthenticated) "$publicFields|$authFields" else publicFields

    /** Fetches a single geocache by its OKAPI cache code (e.g. "OP1234"). */
    suspend fun get(
        code: String,
        langpref: String? = null,
        lpc: Int? = null,
        myLocation: String? = null,
        userUuid: String? = null,
        attributionAppend: String? = null,
        ocTeamAnnotation: String? = null,
        ownerFields: String? = null,
        logFields: String? = null,
        logUserFields: String? = null,
        userLogsOnly: Boolean = false,
    ): Geocache {
        val params = buildMap {
            put("cache_code", code)
            put("fields", defaultFields)
            langpref?.let { put("langpref", it) }
            lpc?.let { put("lpc", it.toString()) }
            myLocation?.let { put("my_location", it) }
            userUuid?.let { put("user_uuid", it) }
            attributionAppend?.let { put("attribution_append", it) }
            ocTeamAnnotation?.let { put("oc_team_annotation", it) }
            ownerFields?.let { put("owner_fields", it) }
            logFields?.let { put("log_fields", it) }
            logUserFields?.let { put("log_user_fields", it) }
            if (userLogsOnly) put("user_logs_only", "true")
        }
        val body = engine.get("caches/geocache", params)
        return json.decodeFromString<GeocacheDto>(body).toDomain()
    }

    /** Fetches multiple geocaches by their cache codes in a single request. */
    suspend fun getMultiple(
        codes: List<String>,
        langpref: String? = null,
        lpc: Int? = null,
        myLocation: String? = null,
        userUuid: String? = null,
        attributionAppend: String? = null,
        ocTeamAnnotation: String? = null,
        ownerFields: String? = null,
        logFields: String? = null,
        logUserFields: String? = null,
        userLogsOnly: Boolean = false,
    ): List<Geocache> {
        val params = buildMap {
            put("cache_codes", codes.joinToString("|"))
            put("fields", defaultFields)
            langpref?.let { put("langpref", it) }
            lpc?.let { put("lpc", it.toString()) }
            myLocation?.let { put("my_location", it) }
            userUuid?.let { put("user_uuid", it) }
            attributionAppend?.let { put("attribution_append", it) }
            ocTeamAnnotation?.let { put("oc_team_annotation", it) }
            ownerFields?.let { put("owner_fields", it) }
            logFields?.let { put("log_fields", it) }
            logUserFields?.let { put("log_user_fields", it) }
            if (userLogsOnly) put("user_logs_only", "true")
        }
        val body = engine.get("caches/geocaches", params)
        return json.decodeFromString<Map<String, GeocacheDto>>(body).values.map { it.toDomain() }
    }

    /**
     * Marks a cache as watched and/or ignored for the authenticated user.
     *
     * At least one of [watched] or [ignored] must be provided.
     * Requires authentication.
     */
    suspend fun mark(cacheCode: String, watched: Boolean? = null, ignored: Boolean? = null) {
        val params = buildMap {
            put("cache_code", cacheCode)
            watched?.let { put("watched", it.toString()) }
            ignored?.let { put("ignored", it.toString()) }
        }
        engine.post("caches/mark", params)
    }

    /**
     * Saves private personal notes on a cache for the authenticated user.
     *
     * @param oldValue Previous notes value for optimistic concurrency; pass empty string to skip the check.
     * Requires authentication.
     */
    suspend fun savePersonalNotes(cacheCode: String, notes: String, oldValue: String = "") {
        engine.post(
            "caches/save_personal_notes", mapOf(
                "cache_code" to cacheCode,
                "new_value" to notes,
                "old_value" to oldValue,
            )
        )
    }

    /**
     * Triggers the cache-edit flow on the OKAPI site (owner or admin only).
     *
     * @param passwd Password required for password-protected caches.
     * @throws KokapiException.ApiError if the server returns success=false.
     * Requires authentication.
     */
    suspend fun edit(cacheCode: String, passwd: String? = null) {
        val params = buildMap {
            put("cache_code", cacheCode)
            passwd?.let { put("passwd", it) }
        }
        val body = engine.post("caches/edit", params)
        val result = json.decodeFromString<SuccessDto>(body)
        if (!result.success) throw KokapiException.ApiError("edit_failed", "caches/edit returned success=false")
    }
}
