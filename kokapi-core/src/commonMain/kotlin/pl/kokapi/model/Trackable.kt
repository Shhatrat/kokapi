package pl.kokapi.model

/** A trackable item (e.g. Travel Bug) currently placed in a geocache. */
data class Trackable(
    val code: String,
    val name: String,
    val url: String?,
)
