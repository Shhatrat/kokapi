package pl.kokapi.model

import pl.kokapi.model.`enum`.LogType

data class GeocacheLog(
    val uuid: String,
    val cacheCode: String,
    val user: UserRef,
    val type: LogType,
    val date: String,
    val comment: String,
    val images: List<LogImage> = emptyList(),
    val wasRecommended: Boolean = false,
    val needsMaintenance: Boolean? = null,
    val dateCreated: String? = null,
    val lastModified: String? = null,
)
