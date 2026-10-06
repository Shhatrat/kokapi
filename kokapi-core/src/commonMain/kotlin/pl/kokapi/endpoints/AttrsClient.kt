package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.AttributeDto
import pl.kokapi.http.dto.AttributeFullDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Attribute

/** Client for fetching geocache attribute definitions from the OKAPI server. */
class AttrsClient internal constructor(private val engine: HttpEngine) {

    /** Fetches basic info (code + name) for a set of attribute codes. */
    suspend fun attributes(acodes: List<String>): List<Attribute> {
        val body = engine.get(
            "attrs/attributes", mapOf(
                "acodes" to acodes.joinToString("|"),
                "fields" to "acode|name",
            )
        )
        return json.decodeFromString<Map<String, AttributeDto>>(body).values.map { it.toDomain() }
    }

    /** Fetches full details (code, name, and localised name variants) for a single attribute. */
    suspend fun attribute(acode: String): Attribute {
        val body = engine.get(
            "attrs/attribute", mapOf(
                "acode" to acode,
                "fields" to "acode|name|names",
            )
        )
        return json.decodeFromString<AttributeFullDto>(body).toDomain()
    }

    /**
     * Returns the full list of attributes supported by this OKAPI installation.
     *
     * @param onlyLocallyUsed When true, returns only attributes that appear on at least one cache.
     */
    suspend fun attributeIndex(onlyLocallyUsed: Boolean = false): List<Attribute> {
        val params = buildMap {
            put("fields", "acode|name|local_icon_url")
            if (onlyLocallyUsed) put("only_locally_used", "true")
        }
        val body = engine.get("attrs/attribute_index", params)
        return json.decodeFromString<Map<String, AttributeDto>>(body).values.map { it.toDomain() }
    }
}
