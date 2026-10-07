package pl.kokapi.model

/** An image attached to a geocache log entry. */
data class LogImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String?,
    val caption: String?,
    val isSpoiler: Boolean,
)
