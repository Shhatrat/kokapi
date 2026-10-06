package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.ApiStatsDto
import pl.kokapi.http.dto.InstallationDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.ApiStats
import pl.kokapi.model.Installation

/** Client for OKAPI server metadata: installation details and usage statistics. */
class ApiSrvClient internal constructor(private val engine: HttpEngine) {

    /** Returns metadata about the current OKAPI installation (site name, revision, URLs). */
    suspend fun installation(): Installation {
        val body = engine.get(
            "apisrv/installation", mapOf(
                "fields" to "site_url|okapi_base_url|site_name|okapi_revision|registration_url",
            )
        )
        return json.decodeFromString<InstallationDto>(body).toDomain()
    }

    /** Returns the list of all known OKAPI installations across all Opencaching sites. */
    suspend fun installations(): List<Installation> {
        val body = engine.get("apisrv/installations", emptyMap())
        return json.decodeFromString<List<InstallationDto>>(body).map { it.toDomain() }
    }

    /** Returns usage statistics for the current OKAPI installation (request counts, etc.). */
    suspend fun stats(): ApiStats {
        val body = engine.get("apisrv/stats", emptyMap())
        return json.decodeFromString<ApiStatsDto>(body).toDomain()
    }
}
