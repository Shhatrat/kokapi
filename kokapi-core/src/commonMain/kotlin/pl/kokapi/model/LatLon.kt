package pl.kokapi.model

data class LatLon(val lat: Double, val lon: Double)

fun String.toLatLon(): LatLon {
    val parts = split("|")
    require(parts.size == 2) { "Expected 'lat|lon' format, got: $this" }
    return LatLon(parts[0].toDouble(), parts[1].toDouble())
}
