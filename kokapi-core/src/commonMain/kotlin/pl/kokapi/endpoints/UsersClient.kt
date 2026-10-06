package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.UserDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.User

class UsersClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "uuid|username|profile_url|caches_found|hidden_count"

    suspend fun byUsername(username: String): User {
        val body = engine.get(
            "users/by_username", mapOf(
                "username" to username,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<UserDto>(body).toDomain()
    }

    suspend fun byUuid(uuid: String): User {
        val body = engine.get(
            "users/by_uuid", mapOf(
                "user_uuid" to uuid,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<UserDto>(body).toDomain()
    }

    suspend fun me(): User {
        val body = engine.get("users/user", mapOf("fields" to defaultFields))
        return json.decodeFromString<UserDto>(body).toDomain()
    }
}
