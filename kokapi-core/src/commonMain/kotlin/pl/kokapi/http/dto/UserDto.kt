package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDto(
    val uuid: String,
    val username: String,
    @SerialName("profile_url") val profileUrl: String = "",
    @SerialName("caches_found") val cachesFound: Int = 0,
    @SerialName("caches_hidden") val hiddenCount: Int = 0,
    @SerialName("rcmds_given") val rcmdsGiven: Int = 0,
    @SerialName("rcmds_left") val rcmdsLeft: Int? = null,
    @SerialName("home_location") val homeLocation: String? = null,
)
