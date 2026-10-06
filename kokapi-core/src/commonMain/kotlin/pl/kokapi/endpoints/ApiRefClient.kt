package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.ApiMethodDto
import pl.kokapi.http.dto.ApiMethodSummaryDto
import pl.kokapi.http.dto.SuccessDto
import pl.kokapi.http.json
import pl.kokapi.model.ApiMethod
import pl.kokapi.model.ApiMethodSummary

/** Client for OKAPI's self-describing API reference endpoints. */
class ApiRefClient internal constructor(private val engine: HttpEngine) {

    /**
     * Returns the full description and field list for a single OKAPI method.
     *
     * @param methodPath OKAPI method path, e.g. "services/caches/geocache".
     */
    suspend fun method(methodPath: String): ApiMethod {
        val body = engine.get("apiref/method", mapOf("method_path" to methodPath))
        val dto = json.decodeFromString<ApiMethodDto>(body)
        return ApiMethod(
            methodPath = dto.methodPath,
            description = dto.description,
            availableFields = (dto.fields?.optional ?: emptyList()) + (dto.fields?.required ?: emptyList()),
            requiredFields = dto.fields?.required ?: emptyList(),
        )
    }

    /** Returns a summary list of all methods exposed by this OKAPI installation. */
    suspend fun methodIndex(): List<ApiMethodSummary> {
        val body = engine.get("apiref/method_index", emptyMap())
        return json.decodeFromString<List<ApiMethodSummaryDto>>(body).map { dto ->
            ApiMethodSummary(
                methodPath = dto.methodPath,
                description = dto.description,
            )
        }
    }

    /**
     * Submits a bug report for a specific OKAPI method.
     *
     * @param methodPath The method the issue applies to.
     * @param issueDescription Human-readable description of the problem.
     * @return `true` if the server accepted the report.
     */
    suspend fun reportIssue(methodPath: String, issueDescription: String): Boolean {
        val params = mapOf(
            "method_path" to methodPath,
            "issue_description" to issueDescription,
        )
        val body = engine.post("apiref/issue", params)
        return json.decodeFromString<SuccessDto>(body).success
    }
}
