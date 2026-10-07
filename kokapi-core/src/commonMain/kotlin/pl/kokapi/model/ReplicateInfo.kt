package pl.kokapi.model

/** Metadata about the OKAPI replication system. */
data class ReplicateInfo(
    /** Current revision number of the OKAPI database. */
    val revision: Long,
    /** Oldest revision number that [ReplicateClient.changelog] accepts as `since`. */
    val minSince: Long,
    /** Revision number of the latest available full database dump. */
    val latestFulldumpRevision: Long?,
)
