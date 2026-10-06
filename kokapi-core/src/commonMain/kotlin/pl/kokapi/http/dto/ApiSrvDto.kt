package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class InstallationDto(
    @SerialName("site_url") val siteUrl: String = "",
    @SerialName("okapi_base_url") val okapiBaseUrl: String = "",
    @SerialName("site_name") val siteName: String = "",
    @SerialName("okapi_revision") val okapiRevision: Int = 0,
    @SerialName("registration_url") val registrationUrl: String? = null,
)

@Serializable
internal data class ApiStatsDto(
    @SerialName("cache_count") val cacheCount: Int = 0,
)
