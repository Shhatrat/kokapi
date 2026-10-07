package pl.kokapi.model

/** Metadata about the OKAPI replication system (current revision, full dump URL). */
data class ReplicateInfo(
    val revision: Long,
    val fulldumpUrl: String?,
)
