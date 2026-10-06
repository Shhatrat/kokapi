package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocacheLogDto(
    val uuid: String,
    @SerialName("cache_code") val cacheCode: String,
    val user: UserRefDto,
    val type: String,
    val date: String,
    val comment: String = "",
)

@Serializable
internal data class SubmitLogResponseDto(
    val success: Boolean,
    val message: String,
    @SerialName("log_uuid") val logUuid: String? = null,
)
