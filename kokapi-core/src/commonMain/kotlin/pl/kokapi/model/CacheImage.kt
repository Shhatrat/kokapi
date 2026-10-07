package pl.kokapi.model

/** An image attached to a geocache listing. */
data class CacheImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String,
    val caption: String?,
    val isSpoiler: Boolean,
)
