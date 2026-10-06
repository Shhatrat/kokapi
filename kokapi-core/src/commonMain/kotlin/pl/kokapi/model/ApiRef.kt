package pl.kokapi.model

data class ApiMethod(
    val methodPath: String,
    val description: String,
    val availableFields: List<String>,
    val requiredFields: List<String>,
)

data class ApiMethodSummary(
    val methodPath: String,
    val description: String,
)
