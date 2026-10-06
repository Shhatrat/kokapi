package pl.kokapi.endpoints

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.GeocacheDto
import pl.kokapi.http.dto.SearchResultDto
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
    ): List<Geocache> {
        val params = buildMap {
            put("center", "$lat|$lon")
            put("radius", radiusKm.toString())
            put("limit", limit.toString())
            put("offset", offset.toString())
            type?.let { put("type", it) }
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
    ): List<Geocache> {
        val body = engine.get(
            "caches/search/bbox", mapOf(
                "bbox" to "$southLat|$westLon|$northLat|$eastLon",
                "limit" to limit.toString(),
            )
        )
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
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
