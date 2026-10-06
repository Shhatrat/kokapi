package pl.kokapi.model

import pl.kokapi.model.`enum`.LogType

/**
 * A single log entry on a geocache.
 *
 * @property uuid Unique identifier assigned by the OKAPI server.
 * @property cacheCode Code of the cache this log belongs to.
 * @property user The user who submitted the log.
 * @property type Log type (Found, DNF, Comment, etc.).
 * @property date Date the find/visit occurred, in ISO-8601 format.
 * @property wasRecommended Whether the author recommended this cache in the log.
 * @property needsMaintenance Whether the author flagged the cache as needing maintenance.
 */
data class GeocacheLog(
    val uuid: String,
    val cacheCode: String,
    val user: UserRef,
    val type: LogType,
    val date: String,
    val comment: String,
    val images: List<LogImage> = emptyList(),
    val wasRecommended: Boolean = false,
    val needsMaintenance: Boolean? = null,
    val dateCreated: String? = null,
    val lastModified: String? = null,
)
