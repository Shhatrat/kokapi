package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiMethodDto(
    @SerialName("method_path") val methodPath: String = "",
    val description: String = "",
    @SerialName("fields") val fields: ApiMethodFieldsDto? = null,
)

@Serializable
internal data class ApiMethodFieldsDto(
    val required: List<String> = emptyList(),
    val optional: List<String> = emptyList(),
)

@Serializable
internal data class ApiMethodSummaryDto(
    @SerialName("method_path") val methodPath: String = "",
    val description: String = "",
)
