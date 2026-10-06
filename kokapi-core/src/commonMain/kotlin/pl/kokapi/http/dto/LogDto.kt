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
    val images: List<LogImageDto>? = null,
    @SerialName("was_recommended") val wasRecommended: Boolean = false,
    @SerialName("needs_maintenance2") val needsMaintenance: Boolean? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    val location: String? = null,
    @SerialName("oc_team_entry") val ocTeamEntry: Boolean = false,
)

@Serializable
internal data class LogImageDto(
    val uuid: String,
    val url: String,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val caption: String? = null,
    @SerialName("is_spoiler") val isSpoiler: Boolean = false,
)

@Serializable
internal data class SubmitLogResponseDto(
    val success: Boolean,
    val message: String,
    @SerialName("log_uuid") val logUuid: String? = null,
)

@Serializable
internal data class LogCapabilitiesDto(
    @SerialName("can_add") val canAdd: Boolean = false,
    @SerialName("available_log_types") val availableTypes: List<String> = emptyList(),
    @SerialName("requires_password") val requiresPassword: Boolean = false,
    @SerialName("can_recommend") val canRecommend: Boolean = false,
    @SerialName("can_rate") val canRate: Boolean = false,
)

@Serializable
internal data class AddImageResponseDto(
    val success: Boolean = false,
    val message: String = "",
    @SerialName("image_uuid") val imageUuid: String? = null,
)
