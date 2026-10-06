package pl.kokapi.model

data class LogCapabilities(
    val canAdd: Boolean,
    val availableTypes: List<String>,
    val requiresPassword: Boolean,
    val canRecommend: Boolean,
    val canRate: Boolean,
)
