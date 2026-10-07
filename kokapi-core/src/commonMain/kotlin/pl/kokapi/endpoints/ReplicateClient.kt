package pl.kokapi.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.FullDumpDto
import pl.kokapi.http.dto.ReplicateInfoDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.ReplicateInfo

/** The type of object affected by a changelog entry. */
enum class ChangelogObjectType {
    GEOCACHE,
    LOG_ENTRY,
    UNKNOWN,
}

/**
 * The key identifying which object was changed.
 *
 * For `geocache` entries: [code] is set. For `log_entry` entries: [uuid] is set.
 */
@Serializable
data class ChangelogObjectKey(
    val code: String? = null,
    val uuid: String? = null,
)

/** A single change event in the OKAPI replication changelog. */
@Serializable
data class ChangelogEntry(
    /** The type of object this change refers to. */
    @SerialName("object_type") val objectTypeRaw: String = "",
    /** Key identifying which specific object changed. */
    @SerialName("object_key") val objectKey: ChangelogObjectKey = ChangelogObjectKey(),
    /** "replace" = created/updated; "delete" = removed. */
    @SerialName("change_type") val changeType: String = "",
) {
    val objectType: ChangelogObjectType get() = when (objectTypeRaw) {
        "geocache" -> ChangelogObjectType.GEOCACHE
        "log_entry" -> ChangelogObjectType.LOG_ENTRY
        else -> ChangelogObjectType.UNKNOWN
    }
    /** Cache code for GEOCACHE entries. */
    val cacheCode: String? get() = objectKey.code
    /** Log UUID for LOG_ENTRY entries. */
    val logUuid: String? get() = objectKey.uuid
}

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

    /**
     * Returns ALL change events since [sinceRevision], fetching multiple pages automatically.
     *
     * Keeps fetching until `more=false`. Use this when you want a complete picture
     * rather than paging manually.
     */
    suspend fun changelogAll(sinceRevision: Long): Changelog {
        var page = changelog(sinceRevision)
        if (!page.more) return page
        val allEntries = page.changelog.toMutableList()
        var lastRevision = page.revision
        while (page.more) {
            page = changelog(lastRevision)
            allEntries.addAll(page.changelog)
            lastRevision = page.revision
        }
        return Changelog(revision = lastRevision, more = false, changelog = allEntries)
    }

    /** Returns the current revision number of the OKAPI database. */
    suspend fun currentRevision(): Long {
        val body = engine.get("replicate/info", emptyMap())
        return json.decodeFromString<ReplicateInfoDto>(body).changelog.revision
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
