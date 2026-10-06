package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.UserDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.User

class UsersClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "uuid|username|profile_url|caches_found|caches_hidden|rcmds_given|rcmds_left|home_location"

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

    suspend fun byUuids(uuids: List<String>): Map<String, User> {
        val body = engine.get(
            "users/users", mapOf(
                "user_uuids" to uuids.joinToString("|"),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<Map<String, UserDto>>(body).mapValues { it.value.toDomain() }
    }

    suspend fun byUsernames(usernames: List<String>): Map<String, User> {
        val body = engine.get(
            "users/by_usernames", mapOf(
                "usernames" to usernames.joinToString("|"),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<Map<String, UserDto>>(body).mapValues { it.value.toDomain() }
    }

    suspend fun byInternalId(internalId: String): User {
        val body = engine.get(
            "users/by_internal_id", mapOf(
                "internal_id" to internalId,
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<UserDto>(body).toDomain()
    }

    suspend fun byInternalIds(internalIds: List<String>): Map<String, User> {
        val body = engine.get(
            "users/by_internal_ids", mapOf(
                "internal_ids" to internalIds.joinToString("|"),
                "fields" to defaultFields,
            )
        )
        return json.decodeFromString<Map<String, UserDto>>(body).mapValues { it.value.toDomain() }
    }

    suspend fun me(): User {
        val body = engine.get("users/user", mapOf("fields" to defaultFields))
        return json.decodeFromString<UserDto>(body).toDomain()
    }
}
