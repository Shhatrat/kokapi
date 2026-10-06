package pl.kokapi.model

enum class CacheType {
    TRADITIONAL, MULTI, QUIZ, VIRTUAL, EVENT, WEBCAM, MOVING, PODCAST, OWN, OTHER;

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

enum class CacheStatus {
    AVAILABLE, TEMPORARILY_UNAVAILABLE, ARCHIVED, OTHER;

    companion object {
        fun fromString(s: String): CacheStatus = when (s.lowercase()) {
            "available" -> AVAILABLE
            "temporarily unavailable" -> TEMPORARILY_UNAVAILABLE
            "archived" -> ARCHIVED
            else -> OTHER
        }
    }
}

enum class LogType {
    FOUND, NOT_FOUND, COMMENT, ATTENDED, WILL_ATTEND, OTHER;

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
