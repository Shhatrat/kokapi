package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiMethodDto(
    val name: String = "",
    @SerialName("brief_description") val briefDescription: String = "",
    val description: String = "",
    val arguments: List<ApiMethodArgumentDto> = emptyList(),
)

@Serializable
internal data class ApiMethodArgumentDto(
    val name: String = "",
    @SerialName("is_required") val isRequired: Boolean = false,
)

@Serializable
internal data class ApiMethodSummaryDto(
    val name: String = "",
    @SerialName("brief_description") val briefDescription: String = "",
)
