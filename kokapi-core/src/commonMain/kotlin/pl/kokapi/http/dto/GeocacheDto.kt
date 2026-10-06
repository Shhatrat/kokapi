package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocacheDto(
    val code: String,
    val name: String,
    val location: String,
    val type: String,
    val status: String,
    val difficulty: Float = 0f,
    val terrain: Float = 0f,
    val owner: UserRefDto? = null,
    val attributes: List<AttributeDto>? = null,
    val description: String? = null,
    @SerialName("hint2") val hint: String? = null,
    val url: String? = null,
    val founds: Int = 0,
    @SerialName("notfounds") val notFounds: Int = 0,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("last_found") val lastFound: String? = null,
)

@Serializable
internal data class UserRefDto(
    val uuid: String,
    val username: String,
)

@Serializable
internal data class AttributeDto(
    val acode: String,
    val name: String,
)

@Serializable
internal data class SearchResultDto(
    val results: List<String>,
    val more: Boolean,
)

@Serializable
internal data class GeocachesResultDto(
    val results: Map<String, GeocacheDto>,
    val more: Boolean = false,
)
