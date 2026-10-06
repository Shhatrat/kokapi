package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine

/** Client for exporting geocaches in GPS device formats and fetching map tiles. */
class FormattersClient internal constructor(private val engine: HttpEngine) {

    /**
     * Exports one or more caches as a GPX string.
     *
     * @param cacheCodes Cache codes to include in the export.
     * @param langpref Preferred language code for descriptions (e.g. "en", "pl").
     * @param nsGround Include Groundspeak GPX extension namespace.
     * @param nsOx Include Opencaching GPX extension namespace.
     * @param images Include cache image URLs in the GPX.
     * @param trackables Include trackable items in the GPX.
     * @param altWpts Include alternate waypoints (e.g. parking, stages) in the GPX.
     * @param markFound Mark caches as already found in the output (for supported readers).
     * @return GPX document as a string.
     */
    suspend fun gpx(
        cacheCodes: List<String>,
        langpref: String = "en",
        nsGround: Boolean = true,
        nsOx: Boolean = false,
        images: Boolean = false,
        trackables: Boolean = false,
        altWpts: Boolean = false,
        markFound: Boolean = false,
    ): String {
        val params = mapOf(
            "cache_codes" to cacheCodes.joinToString("|"),
            "langpref" to langpref,
            "ns_ground" to nsGround.toString(),
            "ns_ox" to nsOx.toString(),
            "images" to images.toString(),
            "trackables" to trackables.toString(),
            "alt_wpts" to altWpts.toString(),
            "mark_found" to markFound.toString(),
        )
        return engine.get("caches/formatters/gpx", params)
    }

    /**
     * Exports caches in Garmin's binary POI/GPX format (.gpi).
     *
     * @return Binary Garmin POI file as a [ByteArray].
     */
    suspend fun garmin(
        cacheCodes: List<String>,
        langpref: String = "en",
        images: Boolean = false,
        trackables: Boolean = false,
    ): ByteArray {
        val params = mapOf(
            "cache_codes" to cacheCodes.joinToString("|"),
            "langpref" to langpref,
            "images" to images.toString(),
            "trackables" to trackables.toString(),
        )
        return engine.getBytes("caches/formatters/garmin", params)
    }

    /**
     * Exports caches in the GGZ binary format used by some Garmin devices.
     *
     * @return Binary GGZ file as a [ByteArray].
     */
    suspend fun ggz(
        cacheCodes: List<String>,
        langpref: String = "en",
        images: Boolean = false,
        trackables: Boolean = false,
    ): ByteArray {
        val params = mapOf(
            "cache_codes" to cacheCodes.joinToString("|"),
            "langpref" to langpref,
            "images" to images.toString(),
            "trackables" to trackables.toString(),
        )
        return engine.getBytes("caches/formatters/ggz", params)
    }

    /**
     * Fetches a single slippy-map PNG tile showing cache locations.
     *
     * Uses standard XYZ tile coordinates (same as OpenStreetMap).
     *
     * @param z Zoom level.
     * @param x Tile column.
     * @param y Tile row.
     * @param status Filter displayed caches by status (e.g. "Available").
     * @param type Filter displayed caches by type (e.g. "Traditional").
     * @return PNG image as a [ByteArray].
     */
    suspend fun mapTile(z: Int, x: Int, y: Int, status: String? = null, type: String? = null): ByteArray {
        val params = buildMap {
            put("z", z.toString())
            put("x", x.toString())
            put("y", y.toString())
            status?.let { put("status", it) }
            type?.let { put("type", it) }
        }
        return engine.getBytes("caches/map/tile", params)
    }
}
