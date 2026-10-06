package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AttributeFullDto(
    val acode: String,
    val name: String,
    val names: Map<String, String> = emptyMap(),
    val description: String? = null,
    @SerialName("is_locally_used") val isLocallyUsed: Boolean = true,
    @SerialName("local_icon_url") val localIconUrl: String? = null,
)
