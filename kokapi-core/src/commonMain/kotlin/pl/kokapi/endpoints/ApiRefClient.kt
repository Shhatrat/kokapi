package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.ApiMethodDto
import pl.kokapi.http.dto.ApiMethodSummaryDto
import pl.kokapi.http.dto.SuccessDto
import pl.kokapi.http.json
import pl.kokapi.model.ApiMethod
import pl.kokapi.model.ApiMethodSummary

class ApiRefClient internal constructor(private val engine: HttpEngine) {

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

    suspend fun methodIndex(): List<ApiMethodSummary> {
        val body = engine.get("apiref/method_index", emptyMap())
        return json.decodeFromString<List<ApiMethodSummaryDto>>(body).map { dto ->
            ApiMethodSummary(
                methodPath = dto.methodPath,
                description = dto.description,
            )
        }
    }

    suspend fun reportIssue(methodPath: String, issueDescription: String): Boolean {
        val params = mapOf(
            "method_path" to methodPath,
            "issue_description" to issueDescription,
        )
        val body = engine.post("apiref/issue", params)
        return json.decodeFromString<SuccessDto>(body).success
    }
}
