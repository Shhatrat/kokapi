package pl.kokapi.model

/** Constraints and permissions for submitting a log to a particular cache or the site. */
data class LogCapabilities(
    val canAdd: Boolean,
    val availableTypes: List<String>,
    val requiresPassword: Boolean,
    val canRecommend: Boolean,
    val canRate: Boolean,
)
