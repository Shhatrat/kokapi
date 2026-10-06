package pl.kokapi.model

data class Attribute(
    val acode: String,
    val name: String,
    val isYes: Boolean,
    val iconUrl: String? = null,
    val description: String? = null,
)
