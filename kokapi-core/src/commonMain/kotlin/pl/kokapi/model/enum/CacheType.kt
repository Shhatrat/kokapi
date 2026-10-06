package pl.kokapi.model.enum

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
