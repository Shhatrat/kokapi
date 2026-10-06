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
    fun searchReturnsList() = runTest {
        val searchCodesJson = """{"results":["OP1","OP2"],"more":false}"""
        val geocachesJson = """{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}},"OP2":{"code":"OP2","name":"C2","location":"51.1|20.1","type":"Multi","status":"Available","difficulty":2.0,"terrain":2.0,"owner":{"uuid":"u","username":"u"}}}"""
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { request ->
                val body = if (request.url.encodedPath.contains("search")) searchCodesJson else geocachesJson
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )
        val results = client.caches.search(lat = 51.0, lon = 20.0, radiusKm = 5.0)
        assertEquals(2, results.size)
    }

    @Test
    fun searchAllReturnsList() = runTest {
        val searchCodesJson = """{"results":["OP1"],"more":false}"""
        val geocachesJson = """{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}}}"""
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { request ->
                val body = if (request.url.encodedPath.contains("search")) searchCodesJson else geocachesJson
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )
        val results = client.caches.searchAll()
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
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
    fun searchByUrlsReturnsList() = runTest {
        val searchCodesJson = """{"results":["OP1"],"more":false}"""
        val geocachesJson = """{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}}}"""
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { request ->
                val body = if (request.url.encodedPath.contains("by_urls")) searchCodesJson else geocachesJson
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )
        val results = client.caches.searchByUrls(listOf("https://opencaching.pl/viewcache.php?wp=OP1"))
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
    }

    @Test
    fun searchByUrlsReturnsEmptyListForEmptyResults() = runTest {
        val client = clientWith("""{"results":[],"more":false}""")
        val results = client.caches.searchByUrls(listOf("https://opencaching.pl/viewcache.php?wp=OPXXX"))
        assertEquals(0, results.size)
    }

    @Test
    fun searchSaveReturnsSetId() = runTest {
        val client = clientWith("""{"set_id":"abc123"}""")
        val setId = client.caches.searchSave()
        assertEquals("abc123", setId)
    }

    @Test
    fun searchSaveWithParamsReturnsSetId() = runTest {
        val client = clientWith("""{"set_id":"xyz789"}""")
        val setId = client.caches.searchSave(type = listOf("Traditional"), limit = 50)
        assertEquals("xyz789", setId)
    }

    @Test
    fun searchAndRetrieveReturnsList() = runTest {
        val responseJson = """{"results":{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}}}}"""
        val results = clientWith(responseJson).caches.searchAndRetrieve()
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
    }

    @Test
    fun searchAndRetrieveReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":{}}""").caches.searchAndRetrieve()
        assertEquals(0, results.size)
    }
}
