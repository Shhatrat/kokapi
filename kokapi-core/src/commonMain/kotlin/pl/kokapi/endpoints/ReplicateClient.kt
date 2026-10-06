package pl.kokapi.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.FullDumpDto
import pl.kokapi.http.dto.ReplicateInfoDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.ReplicateInfo

/** A single change event in the OKAPI replication changelog. */
@Serializable
data class ChangelogEntry(
    /** Action type, e.g. "replace" or "delete". */
    val action: String,
    /** Cache code affected by this change, if applicable. */
    @SerialName("cache_code") val cacheCode: String? = null,
    /** Finer-grained change category (e.g. "attr_changed"). */
    @SerialName("change_type") val changeType: String? = null,
)

/** A page of changelog entries returned by [ReplicateClient.changelog]. */
@Serializable
data class Changelog(
    /** Current revision number of the OKAPI database. */
    val revision: Long,
    /** Whether there are more entries after this page. */
    val more: Boolean,
    /** List of change events since the requested revision. */
    val changelog: List<ChangelogEntry>,
)

/**
 * Client for OKAPI database replication and synchronisation.
 *
 * Intended for applications that maintain a local mirror of the OKAPI database
 * and need to track incremental changes or perform full dumps.
 */
class ReplicateClient internal constructor(private val engine: HttpEngine) {

    /**
     * Returns change events that occurred after [sinceRevision].
     *
     * Use [Changelog.more] to detect whether additional pages are available,
     * and [Changelog.revision] to advance the cursor for the next call.
     */
    suspend fun changelog(sinceRevision: Long): Changelog {
        val body = engine.get("replicate/changelog", mapOf("since" to sinceRevision.toString()))
        return json.decodeFromString(body)
    }

    /** Returns the current revision number of the OKAPI database without fetching any entries. */
    suspend fun currentRevision(): Long {
        val body = engine.get("replicate/changelog", mapOf("since" to "0", "limit" to "1"))
        return json.decodeFromString<Changelog>(body).revision
    }

    /** Returns metadata about the replication system (supported revision range, etc.). */
    suspend fun info(): ReplicateInfo {
        val body = engine.get("replicate/info", emptyMap())
        return json.decodeFromString<ReplicateInfoDto>(body).toDomain()
    }

    /**
     * Returns the URL of the current full database dump archive.
     *
     * Download the archive to bootstrap a local mirror without paging through all changelog entries.
     *
     * @return URL string pointing to the dump file.
     */
    suspend fun fullDump(): String {
        val body = engine.get("replicate/fulldump", emptyMap())
        return json.decodeFromString<FullDumpDto>(body).url
    }
}
