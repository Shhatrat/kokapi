package pl.kokapi.model.enum

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
