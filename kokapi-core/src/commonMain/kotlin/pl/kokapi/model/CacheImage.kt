package pl.kokapi.model

data class CacheImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String,
    val caption: String?,
    val isSpoiler: Boolean,
)
