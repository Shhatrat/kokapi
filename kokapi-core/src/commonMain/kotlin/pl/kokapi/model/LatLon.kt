package pl.kokapi.model

/** A geographic coordinate pair. */
data class LatLon(val lat: Double, val lon: Double)

/**
 * Parses a coordinate string in OKAPI's "lat|lon" format into a [LatLon].
 *
 * @throws IllegalArgumentException if the string is not in "lat|lon" format.
 */
fun String.toLatLon(): LatLon {
    val parts = split("|")
    require(parts.size == 2) { "Expected 'lat|lon' format, got: $this" }
    return LatLon(parts[0].toDouble(), parts[1].toDouble())
}
