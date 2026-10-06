package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.CacheStatus
import pl.kokapi.model.CacheType
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

    private val searchAndRetrieveJson = """
        {
          "results": {
            "OP1": {"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"},"attributes":[]},
            "OP2": {"code":"OP2","name":"C2","location":"51.1|20.1","type":"Multi","status":"Available","difficulty":2.0,"terrain":2.0,"owner":{"uuid":"u","username":"u"},"attributes":[]}
          },
          "more": false
        }
    """.trimIndent()

    @Test
    fun searchReturnsList() = runTest {
        val results = clientWith(searchAndRetrieveJson).caches.search(
            lat = 51.0, lon = 20.0, radiusKm = 5.0
        )
        assertEquals(2, results.size)
    }
}
