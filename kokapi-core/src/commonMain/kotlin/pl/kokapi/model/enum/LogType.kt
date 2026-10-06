package pl.kokapi.model.enum

/**
 * Type of a geocache log entry, corresponding to OKAPI's log type strings.
 *
 * [OTHER] maps to a plain Comment on submission and is the fallback for unrecognised values.
 *
 * @property okapiName The exact string sent to / received from the OKAPI server.
 */
enum class LogType {
    /** The cacher found the physical container. */
    FOUND,

    /** The cacher searched but could not locate the container. */
    NOT_FOUND,

    /** A text note without a find or DNF claim. */
    COMMENT,

    /** Used for event caches — the cacher attended the event. */
    ATTENDED,

    /** Used for event caches — the cacher plans to attend. */
    WILL_ATTEND,

    /** Fallback for unrecognised log type strings; submitted as a Comment. */
    OTHER;

    val okapiName: String
        get() = when (this) {
            FOUND -> "Found it"
            NOT_FOUND -> "Didn't find it"
            COMMENT -> "Comment"
            ATTENDED -> "Attended"
            WILL_ATTEND -> "Will attend"
            OTHER -> "Comment"
        }

    companion object {
        fun fromString(s: String): LogType = when (s.lowercase()) {
            "found it" -> FOUND
            "didn't find it" -> NOT_FOUND
            "comment" -> COMMENT
            "attended" -> ATTENDED
            "will attend" -> WILL_ATTEND
            else -> OTHER
        }
    }
}
