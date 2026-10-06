package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine

class FormattersClient internal constructor(private val engine: HttpEngine) {

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
