package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.ApiStatsDto
import pl.kokapi.http.dto.InstallationDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.ApiStats
import pl.kokapi.model.Installation

class ApiSrvClient internal constructor(private val engine: HttpEngine) {

    suspend fun installation(): Installation {
        val body = engine.get(
            "apisrv/installation", mapOf(
                "fields" to "site_url|okapi_base_url|site_name|okapi_revision|registration_url",
            )
        )
        return json.decodeFromString<InstallationDto>(body).toDomain()
    }

    suspend fun installations(): List<Installation> {
        val body = engine.get("apisrv/installations", emptyMap())
        return json.decodeFromString<List<InstallationDto>>(body).map { it.toDomain() }
    }

    suspend fun stats(): ApiStats {
        val body = engine.get("apisrv/stats", emptyMap())
        return json.decodeFromString<ApiStatsDto>(body).toDomain()
    }
}
