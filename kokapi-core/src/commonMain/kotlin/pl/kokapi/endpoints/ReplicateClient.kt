package pl.kokapi.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.FullDumpDto
import pl.kokapi.http.dto.ReplicateInfoDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.ReplicateInfo

@Serializable
data class ChangelogEntry(
    val action: String,
    @SerialName("cache_code") val cacheCode: String? = null,
    @SerialName("change_type") val changeType: String? = null,
)

@Serializable
data class Changelog(
    val revision: Long,
    val more: Boolean,
    val changelog: List<ChangelogEntry>,
)

class ReplicateClient internal constructor(private val engine: HttpEngine) {

    suspend fun changelog(sinceRevision: Long): Changelog {
        val body = engine.get("replicate/changelog", mapOf("since" to sinceRevision.toString()))
        return json.decodeFromString(body)
    }

    suspend fun currentRevision(): Long {
        val body = engine.get("replicate/changelog", mapOf("since" to "0", "limit" to "1"))
        return json.decodeFromString<Changelog>(body).revision
    }

    suspend fun info(): ReplicateInfo {
        val body = engine.get("replicate/info", emptyMap())
        return json.decodeFromString<ReplicateInfoDto>(body).toDomain()
    }

    suspend fun fullDump(): String {
        val body = engine.get("replicate/fulldump", emptyMap())
        return json.decodeFromString<FullDumpDto>(body).url
    }
}
