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
}
