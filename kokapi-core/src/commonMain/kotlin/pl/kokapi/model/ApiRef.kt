package pl.kokapi.model

/** Full description of a single OKAPI method, including available and required fields. */
data class ApiMethod(
    val methodPath: String,
    val description: String,
    val availableFields: List<String>,
    val requiredFields: List<String>,
)

/** Brief summary of a single OKAPI method as returned by the method index. */
data class ApiMethodSummary(
    val methodPath: String,
    val description: String,
)
