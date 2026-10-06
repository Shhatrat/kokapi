package pl.kokapi.model

data class LogImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String?,
    val caption: String?,
    val isSpoiler: Boolean,
)
