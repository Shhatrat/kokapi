package pl.kokapi.model

data class AltWaypoint(
    val name: String,
    val location: LatLon,
    val type: String,
    val typeName: String,
    val description: String?,
)
