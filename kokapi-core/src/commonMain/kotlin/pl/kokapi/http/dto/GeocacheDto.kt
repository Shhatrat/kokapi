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
    @SerialName("gc_code") val gcCode: String? = null,
    val size2: String? = null,
    val recommendations: Int = 0,
    @SerialName("req_passwd") val reqPasswd: Boolean = false,
    @SerialName("short_description") val shortDescription: String? = null,
    val images: List<CacheImageDto>? = null,
    @SerialName("trackables_count") val trackablesCount: Int = 0,
    val trackables: List<TrackableDto>? = null,
    @SerialName("alt_wpts") val altWpts: List<AltWaypointDto>? = null,
    @SerialName("country2") val country: String? = null,
    val region: String? = null,
    val rating: Float? = null,
    @SerialName("trip_time") val tripTime: Float? = null,
    @SerialName("trip_distance") val tripDistance: Float? = null,
    @SerialName("date_hidden") val dateHidden: String? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    @SerialName("is_found") val isFound: Boolean? = null,
    @SerialName("is_watched") val isWatched: Boolean? = null,
)

@Serializable
internal data class CacheImageDto(
    val uuid: String,
    val url: String,
    @SerialName("thumb_url") val thumbUrl: String = "",
    val caption: String? = null,
    @SerialName("is_spoiler") val isSpoiler: Boolean = false,
)

@Serializable
internal data class TrackableDto(
    val code: String,
    val name: String,
    val url: String? = null,
)

@Serializable
internal data class AltWaypointDto(
    val name: String,
    val location: String,
    val type: String,
    @SerialName("type_name") val typeName: String = "",
    val description: String? = null,
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
    @SerialName("local_icon_url") val iconUrl: String? = null,
    val description: String? = null,
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

@Serializable
internal data class SearchSaveResultDto(
    @SerialName("set_id") val setId: String,
)

@Serializable
internal data class SearchAndRetrieveResultDto(
    val results: Map<String, GeocacheDto>,
)

@Serializable
internal data class SuccessDto(
    val success: Boolean,
)
