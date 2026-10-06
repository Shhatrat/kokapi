package pl.kokapi.model.enum

/**
 * Type of a geocache, corresponding to OKAPI's "type" field values.
 *
 * [OTHER] is used as a fallback for any type string not recognised by this enum.
 */
enum class CacheType {
    /** Classic single-stage cache with coordinates leading directly to the container. */
    TRADITIONAL,

    /** Multi-stage cache where each stage reveals coordinates of the next. */
    MULTI,

    /** Puzzle/mystery cache requiring solving a riddle to find the final coordinates. */
    QUIZ,

    /** Cache with no physical container; the "find" is the location itself. */
    VIRTUAL,

    /** Organised event at a specific time and place. */
    EVENT,

    /** Cache found by pointing a webcam at yourself or a specific target. */
    WEBCAM,

    /** A cache that physically moves between locations. */
    MOVING,

    /** Audio-based cache (podcast format). */
    PODCAST,

    /** Special type used for the owner's own listing view. */
    OWN,

    /** Fallback for unrecognised or future type strings. */
    OTHER;

    companion object {
        fun fromString(s: String): CacheType = when (s.lowercase()) {
            "traditional" -> TRADITIONAL
            "multi" -> MULTI
            "quiz" -> QUIZ
            "virtual" -> VIRTUAL
            "event" -> EVENT
            "webcam" -> WEBCAM
            "moving" -> MOVING
            "podcast" -> PODCAST
            "own" -> OWN
            else -> OTHER
        }
    }
}
