package pl.kokapi.model

data class GeocacheLog(
    val uuid: String,
    val cacheCode: String,
    val user: UserRef,
    val type: LogType,
    val date: String,
    val comment: String,
)
