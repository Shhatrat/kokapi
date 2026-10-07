package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReplicateInfoDto(
    val changelog: ReplicateChangelogMetaDto = ReplicateChangelogMetaDto(),
    @SerialName("latest_fulldump") val latestFulldump: ReplicateFulldumpMetaDto? = null,
)

@Serializable
internal data class ReplicateChangelogMetaDto(
    val revision: Long = 0L,
    @SerialName("min_since") val minSince: Long = 0L,
)

@Serializable
internal data class ReplicateFulldumpMetaDto(
    val revision: Long = 0L,
    @SerialName("generated_at") val generatedAt: String = "",
    val size: Long = 0L,
)

@Serializable
internal data class FullDumpDto(
    val url: String = "",
)
