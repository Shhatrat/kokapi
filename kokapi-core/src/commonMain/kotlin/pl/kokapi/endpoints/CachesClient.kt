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

class CachesClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields =
        "code|name|location|type|status|difficulty|terrain|owner|description|hint2|url|founds|notfounds|date_created|last_found"

    suspend fun get(code: String): Geocache {
        val body = engine.get(
            "caches/geocache", mapOf(
                "cache_code" to code,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<GeocacheDto>(body).toDomain()
    }

    suspend fun getMultiple(codes: List<String>): List<Geocache> {
        val body = engine.get(
            "caches/geocaches", mapOf(
                "cache_codes" to codes.joinToString("|"),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<Map<String, GeocacheDto>>(body).values.map { it.toDomain() }
    }

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
        foundStatus: String? = null,
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
            foundStatus?.let { put("found_status", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/nearest", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

    suspend fun bbox(
        southLat: Double,
        westLon: Double,
        northLat: Double,
        eastLon: Double,
        limit: Int = 100,
        offset: Int = 0,
        orderBy: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("bbox", "$southLat|$westLon|$northLat|$eastLon")
            put("limit", limit.toString())
            put("offset", offset.toString())
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/bbox", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

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
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
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
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/all", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

    suspend fun mark(cacheCode: String, watched: Boolean? = null, ignored: Boolean? = null) {
        val params = buildMap {
            put("cache_code", cacheCode)
            watched?.let { put("watched", it.toString()) }
            ignored?.let { put("ignored", it.toString()) }
        }
        engine.post("caches/mark", params)
    }

    suspend fun savePersonalNotes(cacheCode: String, notes: String, oldValue: String = "") {
        engine.post(
            "caches/save_personal_notes", mapOf(
                "cache_code" to cacheCode,
                "new_value" to notes,
                "old_value" to oldValue,
            )
        )
    }

    suspend fun edit(cacheCode: String, passwd: String? = null) {
        val params = buildMap {
            put("cache_code", cacheCode)
            passwd?.let { put("passwd", it) }
        }
        val body = engine.post("caches/edit", params)
        val result = json.decodeFromString<SuccessDto>(body)
        if (!result.success) throw KokapiException.ApiError("edit_failed", "caches/edit returned success=false")
    }

    suspend fun searchByUrls(urls: List<String>): List<Geocache> {
        val params = mapOf("urls" to urls.joinToString("|"))
        val body = engine.get("caches/search/by_urls", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }

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
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
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
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/save", params)
        return json.decodeFromString<SearchSaveResultDto>(body).setId
    }

    suspend fun searchAndRetrieve(
        type: List<String>? = null,
        status: List<String> = listOf("Available"),
        terrain: String? = null,
        difficulty: String? = null,
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
        )
        val body = engine.get("caches/shortcuts/search_and_retrieve", params)
        val result = json.decodeFromString<SearchAndRetrieveResultDto>(body)
        return result.results.values.map { it.toDomain() }
    }

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
