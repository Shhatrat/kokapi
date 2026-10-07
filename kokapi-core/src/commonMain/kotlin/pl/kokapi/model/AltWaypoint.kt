package pl.kokapi.model

/** An alternate waypoint associated with a geocache (e.g. parking, final stage). */
data class AltWaypoint(
    val name: String,
    val location: LatLon,
    val type: String,
    val typeName: String,
    val description: String?,
)
