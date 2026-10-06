package pl.kokapi.model.enum

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
