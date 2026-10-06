package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class UsersClientTest {

    private fun clientWith(body: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    private val userJson = """
        {"uuid":"u1","username":"tester","profile_url":"https://opencaching.pl/user/tester","caches_found":42,"hidden_count":5}
    """.trimIndent()

    @Test
    fun byUsernameReturnsUser() = runTest {
        val user = clientWith(userJson).users.byUsername("tester")
        assertEquals("u1", user.uuid)
        assertEquals("tester", user.username)
        assertEquals(42, user.cachesFound)
    }

    @Test
    fun byUuidReturnsUser() = runTest {
        val user = clientWith(userJson).users.byUuid("u1")
        assertEquals("tester", user.username)
    }

    @Test
    fun byUuidsReturnsMap() = runTest {
        val json = """{"u1":{"uuid":"u1","username":"alice"},"u2":{"uuid":"u2","username":"bob"}}"""
        val users = clientWith(json).users.byUuids(listOf("u1", "u2"))
        assertEquals(2, users.size)
        assertEquals("alice", users["u1"]?.username)
        assertEquals("bob", users["u2"]?.username)
    }

    @Test
    fun byUsernamesReturnsMap() = runTest {
        val json = """{"alice":{"uuid":"u1","username":"alice"},"bob":{"uuid":"u2","username":"bob"}}"""
        val users = clientWith(json).users.byUsernames(listOf("alice", "bob"))
        assertEquals(2, users.size)
        assertEquals("u1", users["alice"]?.uuid)
        assertEquals("u2", users["bob"]?.uuid)
    }

    @Test
    fun byInternalIdReturnsUser() = runTest {
        val json = """{"uuid":"u1","username":"alice"}"""
        val user = clientWith(json).users.byInternalId("123")
        assertEquals("u1", user.uuid)
        assertEquals("alice", user.username)
    }

    @Test
    fun byInternalIdsReturnsMap() = runTest {
        val json = """{"123":{"uuid":"u1","username":"alice"},"456":{"uuid":"u2","username":"bob"}}"""
        val users = clientWith(json).users.byInternalIds(listOf("123", "456"))
        assertEquals(2, users.size)
        assertEquals("u1", users["123"]?.uuid)
    }

    @Test
    fun meReturnsCurrentUser() = runTest {
        val json = """{"uuid":"u1","username":"alice"}"""
        val user = clientWith(json).users.me()
        assertEquals("u1", user.uuid)
        assertEquals("alice", user.username)
    }
}
