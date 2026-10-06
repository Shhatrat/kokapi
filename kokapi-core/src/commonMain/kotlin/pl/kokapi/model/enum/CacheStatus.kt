package pl.kokapi.model.enum

/**
 * Publication status of a geocache.
 *
 * [OTHER] is used as a fallback for any status string not recognised by this enum.
 */
enum class CacheStatus {
    /** Cache is published and actively in play. */
    AVAILABLE,

    /** Cache has a temporary problem (e.g. missing lid, flooded area) but is not archived. */
    TEMPORARILY_UNAVAILABLE,

    /** Cache has been permanently retired and removed from active play. */
    ARCHIVED,

    /** Fallback for unrecognised or future status strings. */
    OTHER;

    companion object {
        fun fromString(s: String): CacheStatus = when (s.lowercase()) {
            "available" -> AVAILABLE
            "temporarily unavailable" -> TEMPORARILY_UNAVAILABLE
            "archived" -> ARCHIVED
            else -> OTHER
        }
    }
}
