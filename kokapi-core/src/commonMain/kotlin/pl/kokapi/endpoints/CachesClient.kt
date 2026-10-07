package pl.kokapi.endpoints

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.GeocacheDto
import pl.kokapi.http.dto.SearchResultDto
import pl.kokapi.http.dto.SearchSaveResultDto
import pl.kokapi.http.dto.SearchAndRetrieveResultDto
import pl.kokapi.http.dto.SuccessDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Geocache

/** Client for geocache search, retrieval, and cache management endpoints. */
class CachesClient internal constructor(private val engine: HttpEngine) {

    private val publicFields =
        "code|name|location|type|status|difficulty|terrain|owner|description|hint2|url|" +
        "founds|notfounds|date_created|last_found|gc_code|size2|recommendations|req_passwd|" +
        "short_description|images|trackables_count|trackables|alt_wpts|country2|region|rating|" +
        "trip_time|trip_distance|date_hidden|last_modified|willattends|watchers|rating_votes|" +
        "needs_maintenance|latest_logs|names|descriptions|short_descriptions|hints2|attr_acodes|" +
        "oc_team_annotation|attribution_note|internal_id|oxsize|protection_areas"

    private val authFields = "is_found|is_not_found|is_watched|is_ignored|is_recommended|my_rating|my_notes"

    private val defaultFields get() =
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
     * Nearest-caches search — finds caches within [radiusKm] of a given point.
     *
     * @param lat Latitude of the search center.
     * @param lon Longitude of the search center.
     * @param radiusKm Search radius in kilometers.
     * @param foundStatus Filter by found status, e.g. "found_only" or "notfound_only".
     */
    suspend fun search(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        limit: Int = 100,
        offset: Int = 0,
        type: String? = null,
        status: String = "Available",
        difficulty: String? = null,
        terrain: String? = null,
        size2: List<String>? = null,
        ownerUuid: String? = null,
        name: String? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("center", "$lat|$lon")
            put("radius", radiusKm.toString())
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("status", status)
            type?.let { put("type", it) }
            difficulty?.let { put("difficulty", it) }
            terrain?.let { put("terrain", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it) }
            name?.let { put("name", it) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/nearest", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

    /**
     * Bounding-box search — finds caches within a geographic rectangle.
     *
     * @param southLat Southern boundary latitude.
     * @param westLon Western boundary longitude.
     * @param northLat Northern boundary latitude.
     * @param eastLon Eastern boundary longitude.
     */
    suspend fun bbox(
        southLat: Double,
        westLon: Double,
        northLat: Double,
        eastLon: Double,
        limit: Int = 100,
        offset: Int = 0,
        type: String? = null,
        status: String = "Available",
        difficulty: String? = null,
        terrain: String? = null,
        size2: List<String>? = null,
        ownerUuid: String? = null,
        name: String? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("bbox", "$southLat|$westLon|$northLat|$eastLon")
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("status", status)
            type?.let { put("type", it) }
            difficulty?.let { put("difficulty", it) }
            terrain?.let { put("terrain", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it) }
            name?.let { put("name", it) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/bbox", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

    /**
     * Full-featured search with 25+ filter options across the entire site.
     *
     * Unlike [search], this is not limited to a radius — it scans the whole site.
     *
     * @param excludeMyOwn Exclude caches owned by the authenticated user.
     * @param withTrackablesOnly Only return caches that currently have trackables.
     * @param modifiedSince ISO-8601 timestamp; return only caches changed after this date.
     * @param foundStatus Filter by found status, e.g. "found_only" or "notfound_only".
     */
    suspend fun searchAll(
        type: List<String>? = null,
        status: List<String> = listOf("Available"),
        ownerUuid: List<String>? = null,
        name: String? = null,
        terrain: String? = null,
        difficulty: String? = null,
        size2: List<String>? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
        limit: Int = 100,
        offset: Int = 0,
    ): List<Geocache> {
        val params = buildMap {
            put("status", status.joinToString("|"))
            put("limit", limit.toString())
            put("offset", offset.toString())
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            type?.let { put("type", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it.joinToString("|")) }
            name?.let { put("name", it) }
            terrain?.let { put("terrain", it) }
            difficulty?.let { put("difficulty", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/all", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
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

    /**
     * Finds caches by their full opencaching site URLs.
     *
     * Useful for resolving cache links shared by users.
     */
    suspend fun searchByUrls(urls: List<String>): List<Geocache> {
        val params = mapOf("urls" to urls.joinToString("|"))
        val body = engine.get("caches/search/by_urls", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

    /**
     * Executes a search on the server and returns a persistent search set ID.
     *
     * The returned ID can be reused to page through large result sets without repeating the query.
     *
     * @return The server-assigned search set ID.
     */
    suspend fun searchSave(
        type: List<String>? = null,
        status: List<String> = listOf("Available"),
        ownerUuid: List<String>? = null,
        name: String? = null,
        terrain: String? = null,
        difficulty: String? = null,
        size2: List<String>? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
        limit: Int = 100,
        offset: Int = 0,
    ): String {
        val params = buildMap {
            put("status", status.joinToString("|"))
            put("limit", limit.toString())
            put("offset", offset.toString())
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            type?.let { put("type", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it.joinToString("|")) }
            name?.let { put("name", it) }
            terrain?.let { put("terrain", it) }
            difficulty?.let { put("difficulty", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/save", params)
        return json.decodeFromString<SearchSaveResultDto>(body).setId
    }

    /**
     * Single-call shortcut that searches and retrieves full geocache data in one request.
     *
     * Combines `caches/search/all` + `caches/geocaches` into a single OKAPI call, reducing round-trips.
     */
    suspend fun searchAndRetrieve(
        type: List<String>? = null,
        status: List<String> = listOf("Available"),
        terrain: String? = null,
        difficulty: String? = null,
        size2: List<String>? = null,
        ownerUuid: List<String>? = null,
        name: String? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
        limit: Int = 100,
        offset: Int = 0,
    ): List<Geocache> {
        val searchParamsMap = buildMap {
            put("status", status.joinToString("|"))
            put("limit", limit.toString())
            put("offset", offset.toString())
            type?.let { put("type", it.joinToString("|")) }
            terrain?.let { put("terrain", it) }
            difficulty?.let { put("difficulty", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it.joinToString("|")) }
            name?.let { put("name", it) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val searchParamsJson = buildString {
            append("{")
            searchParamsMap.entries.forEachIndexed { i, (k, v) ->
                if (i > 0) append(",")
                append("\"$k\":\"$v\"")
            }
            append("}")
        }
        val retrParamsJson = """{"fields":"$defaultFields"}"""
        val params = mapOf(
            "retr_method" to "services/caches/geocaches",
            "retr_params" to retrParamsJson,
            "search_method" to "services/caches/search/all",
            "search_params" to searchParamsJson,
            "wrap" to "true",
        )
        val body = engine.get("caches/shortcuts/search_and_retrieve", params)
        val result = json.decodeFromString<SearchAndRetrieveResultDto>(body)
        return result.results.values.map { it.toDomain() }
    }

    /**
     * Kotlin Flow that paginates through a nearest-caches search automatically.
     *
     * Emits each [Geocache] individually. Pagination stops when a page returns fewer
     * items than [pageSize].
     *
     * @param pageSize Number of caches fetched per page.
     */
    fun searchFlow(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        pageSize: Int = 100,
    ): Flow<Geocache> = flow {
        var offset = 0
        while (true) {
            val page = search(lat, lon, radiusKm, pageSize, offset)
            page.forEach { emit(it) }
            if (page.size < pageSize) break
            offset += pageSize
        }
    }
}
