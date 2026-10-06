package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReplicateInfoDto(
    val revision: Long = 0L,
    @SerialName("fulldump_url") val fulldumpUrl: String? = null,
)

@Serializable
internal data class FullDumpDto(
    val url: String = "",
)
