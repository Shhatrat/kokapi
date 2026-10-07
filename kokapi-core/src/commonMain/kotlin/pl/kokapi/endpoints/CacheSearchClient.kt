package pl.kokapi.endpoints

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.SearchAndRetrieveResultDto
import pl.kokapi.http.dto.SearchResultDto
import pl.kokapi.http.dto.SearchSaveResultDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Geocache

/** Client for geocache discovery — searching, filtering, and paging across the OKAPI database. */
class CacheSearchClient internal constructor(
    private val engine: HttpEngine,
    private val caches: CachesClient,
) {
    private val defaultFields get() = caches.defaultFields

    /**
     * Nearest-caches search — finds caches within [radiusKm] of a given point.
     *
     * @param lat Latitude of the search center.
     * @param lon Longitude of the search center.
     * @param radiusKm Search radius in kilometers.
     * @param filters Additional filter and pagination options.
     */
    suspend fun nearest(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        filters: SearchFilters = SearchFilters(),
    ): List<Geocache> {
        val params = buildMap {
            putAll(filters.toParams())
            put("center", "$lat|$lon")
            put("radius", radiusKm.toString())
        }
        val body = engine.get("caches/search/nearest", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return caches.getMultiple(codes)
    }

    /**
     * Bounding-box search — finds caches within a geographic rectangle.
     *
     * @param southLat Southern boundary latitude.
     * @param westLon Western boundary longitude.
     * @param northLat Northern boundary latitude.
     * @param eastLon Eastern boundary longitude.
     * @param filters Additional filter and pagination options.
     */
    suspend fun bbox(
        southLat: Double,
        westLon: Double,
        northLat: Double,
        eastLon: Double,
        filters: SearchFilters = SearchFilters(),
    ): List<Geocache> {
        val params = buildMap {
            putAll(filters.toParams())
            put("bbox", "$southLat|$westLon|$northLat|$eastLon")
        }
        val body = engine.get("caches/search/bbox", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return caches.getMultiple(codes)
    }

    /**
     * Full-featured search across the entire site.
     *
     * Unlike [nearest], not limited to a radius around a point.
     *
     * @param filters Filter and pagination options.
     */
    suspend fun all(filters: SearchFilters = SearchFilters()): List<Geocache> {
        val body = engine.get("caches/search/all", filters.toParams())
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return caches.getMultiple(codes)
    }

    /**
     * Finds caches by their full opencaching site URLs.
     *
     * Useful for resolving cache links shared by users.
     */
    suspend fun byUrls(urls: List<String>): List<Geocache> {
        val body = engine.get("caches/search/by_urls", mapOf("urls" to urls.joinToString("|")))
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return caches.getMultiple(codes)
    }

    /**
     * Executes a search on the server and returns a persistent search set ID.
     *
     * The returned ID can be reused to page through large result sets without repeating the query.
     *
     * @return The server-assigned search set ID.
     */
    suspend fun save(filters: SearchFilters = SearchFilters()): String {
        val body = engine.get("caches/search/save", filters.toParams())
        return json.decodeFromString<SearchSaveResultDto>(body).setId
    }

    /**
     * Single-call shortcut that searches and retrieves full geocache data in one request.
     *
     * Combines `caches/search/all` + `caches/geocaches` into a single OKAPI call, reducing round-trips.
     */
    suspend fun andRetrieve(filters: SearchFilters = SearchFilters()): List<Geocache> {
        val searchParamsMap = filters.toParams()
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
        return json.decodeFromString<SearchAndRetrieveResultDto>(body).results.values.map { it.toDomain() }
    }

    /**
     * Kotlin Flow that paginates through a nearest-caches search automatically.
     *
     * Emits each [Geocache] individually. Pagination stops when a page returns fewer
     * items than [pageSize].
     *
     * @param pageSize Number of caches fetched per page.
     * @param filters Additional filter options (limit and offset are managed internally).
     */
    fun flow(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        pageSize: Int = 100,
        filters: SearchFilters = SearchFilters(),
    ): Flow<Geocache> = flow {
        var offset = 0
        while (true) {
            val page = nearest(lat, lon, radiusKm, filters.copy(limit = pageSize, offset = offset))
            page.forEach { emit(it) }
            if (page.size < pageSize) break
            offset += pageSize
        }
    }
}
