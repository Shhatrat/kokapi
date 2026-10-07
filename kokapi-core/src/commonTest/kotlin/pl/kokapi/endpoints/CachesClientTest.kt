package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.enum.CacheStatus
import pl.kokapi.model.enum.CacheType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CachesClientTest {

    private fun clientWith(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK) =
        KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine {
                respond(responseBody, status, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )

    private val geocacheJson = """
        {
          "code": "OP1234",
          "name": "Test Cache",
          "location": "50.1234|20.5678",
          "type": "Traditional",
          "status": "Available",
          "difficulty": 2.0,
          "terrain": 1.5,
          "owner": {"uuid": "u1", "username": "tester"},
          "attributes": []
        }
    """.trimIndent()

    @Test
    fun getCacheById() = runTest {
        val cache = clientWith(geocacheJson).caches.get("OP1234")
        assertEquals("OP1234", cache.code)
        assertEquals("Test Cache", cache.name)
        assertEquals(50.1234, cache.location.lat, 0.0001)
        assertEquals(CacheType.TRADITIONAL, cache.type)
        assertEquals(CacheStatus.AVAILABLE, cache.status)
        assertEquals(2.0f, cache.difficulty)
    }

    @Test
    fun getCacheThrowsApiErrorOn400() = runTest {
        val errorJson = """{"error":"cache_not_found","developer_message":"Cache not found"}"""
        assertFailsWith<KokapiException.ApiError> {
            clientWith(errorJson, HttpStatusCode.BadRequest).caches.get("INVALID")
        }
    }

    @Test
    fun getCacheThrowsNetworkErrorOnException() = runTest {
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { throw RuntimeException("Network down") })
        )
        assertFailsWith<KokapiException.NetworkError> {
            client.caches.get("OP1234")
        }
    }

    @Test
    fun getWithOptionalParams() = runTest {
        val cache = clientWith("""{"code":"OP1","name":"T","location":"52.0|21.0","type":"Traditional","status":"Available"}""")
            .caches.get("OP1", langpref = "pl", lpc = 5, userLogsOnly = true)
        assertEquals("OP1", cache.code)
    }

    @Test
    fun markDoesNotThrow() = runTest {
        clientWith("{}").caches.mark("OP1", watched = true)
    }

    @Test
    fun savePersonalNotesDoesNotThrow() = runTest {
        clientWith("{}").caches.savePersonalNotes("OP1", "My note")
    }

    @Test
    fun editDoesNotThrowOnSuccess() = runTest {
        clientWith("""{"success":true}""").caches.edit("OP1234")
    }

    @Test
    fun editWithPasswdDoesNotThrow() = runTest {
        clientWith("""{"success":true}""").caches.edit("OP1234", passwd = "secret")
    }

    @Test
    fun editThrowsOnSuccessFalse() = runTest {
        assertFailsWith<KokapiException.ApiError> {
            clientWith("""{"success":false}""").caches.edit("OP1234")
        }
    }

    @Test
    fun getCachePopulatesExtendedFields() = runTest {
        val json = """
            {
              "code":"OP1","name":"X","location":"50.0|20.0","type":"Traditional",
              "status":"Available","difficulty":1.0,"terrain":1.0,
              "owner":{"uuid":"u","username":"u"},
              "gc_code":"GC1234","size2":"regular","recommendations":3,
              "req_passwd":true,"willattends":2,"watchers":7,
              "is_found":true,"is_not_found":false,"is_watched":true,"is_ignored":false,
              "rating":4.5,"rating_votes":10,"my_rating":5,
              "my_notes":"secret","needs_maintenance":null,
              "latest_logs":[]
            }
        """.trimIndent()
        val cache = clientWith(json).caches.get("OP1")
        assertEquals("GC1234", cache.gcCode)
        assertEquals("regular", cache.size2)
        assertEquals(3, cache.recommendations)
        assertEquals(true, cache.reqPasswd)
        assertEquals(2, cache.willattends)
        assertEquals(7, cache.watchers)
        assertEquals(true, cache.isFound)
        assertEquals(false, cache.isNotFound)
        assertEquals(true, cache.isWatched)
        assertEquals(false, cache.isIgnored)
        assertEquals(4.5f, cache.rating)
        assertEquals(10, cache.ratingVotes)
        assertEquals(5, cache.myRating)
        assertEquals("secret", cache.myNotes)
    }
}
