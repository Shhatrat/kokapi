package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.AttributeDto
import pl.kokapi.http.dto.AttributeFullDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Attribute

class AttrsClient internal constructor(private val engine: HttpEngine) {

    suspend fun attributes(acodes: List<String>): List<Attribute> {
        val body = engine.get(
            "attrs/attributes", mapOf(
                "acodes" to acodes.joinToString("|"),
                "fields" to "acode|name",
            )
        )
        return json.decodeFromString<Map<String, AttributeDto>>(body).values.map { it.toDomain() }
    }

    suspend fun attribute(acode: String): Attribute {
        val body = engine.get(
            "attrs/attribute", mapOf(
                "acode" to acode,
                "fields" to "acode|name|names",
            )
        )
        return json.decodeFromString<AttributeFullDto>(body).toDomain()
    }
}
