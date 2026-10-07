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

    @Test
    fun searchWithNewFiltersReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").caches.search(
            lat = 51.0, lon = 20.0,
            ownerUuid = "uuid-owner",
            foundBy = "uuid-found",
            recommendedOnly = true,
            excludeMyOwn = true,
            watchedOnly = true,
            ftfHunter = true,
            rating = "3-5",
        )
        assertEquals(0, results.size)
    }

    @Test
    fun bboxWithNewFiltersReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").caches.bbox(
            southLat = 50.0, westLon = 19.0,
            northLat = 51.0, eastLon = 21.0,
            foundBy = "uuid-found",
            recommendedOnly = true,
            notFoundBy = "uuid-notfound",
        )
        assertEquals(0, results.size)
    }

    @Test
    fun searchAllWithNewFiltersReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").caches.searchAll(
            foundBy = "uuid-found",
            notFoundBy = "uuid-notfound",
            recommendedOnly = true,
            recommendedBy = "uuid-rcmd",
            watchedOnly = true,
            ignoredStatus = "unignored_only",
            ftfHunter = true,
            setAnd = "set123",
            rating = "3-5",
        )
        assertEquals(0, results.size)
    }

    @Test
    fun searchSaveWithNewFiltersReturnsSetId() = runTest {
        val setId = clientWith("""{"set_id":"xyz"}""").caches.searchSave(
            foundBy = "uuid-found",
            recommendedOnly = true,
            ftfHunter = true,
        )
        assertEquals("xyz", setId)
    }

    @Test
    fun getWithOptionalParams() = runTest {
        val cache = clientWith("""{"code":"OP1","name":"T","location":"52.0|21.0","type":"Traditional","status":"Available"}""")
            .caches.get("OP1", langpref = "pl", lpc = 5, userLogsOnly = true)
        assertEquals("OP1", cache.code)
    }

    @Test
    fun searchFlowEmitsAllCaches() = runTest {
        var callCount = 0
        // searchFlow calls: search/nearest → geocaches → search/nearest (empty, stops)
        val responses = listOf(
            """{"results":["OP1","OP2"],"more":true}""",
            """{"OP1":{"code":"OP1","name":"A","location":"52.0|21.0","type":"Traditional","status":"Available"},"OP2":{"code":"OP2","name":"B","location":"52.0|21.0","type":"Traditional","status":"Available"}}""",
            """{"results":[],"more":false}""",
        )
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine {
                respond(responses[callCount++], HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )
        val collected = mutableListOf<pl.kokapi.model.Geocache>()
        client.caches.searchFlow(52.0, 21.0, pageSize = 2).collect { collected.add(it) }
        assertEquals(2, collected.size)
        assertEquals("OP1", collected[0].code)
        assertEquals("OP2", collected[1].code)
    }

    @Test
    fun searchAndRetrieveWithFullFilters() = runTest {
        val json = """{"results":{"OP1":{"code":"OP1","name":"T","location":"52.0|21.0","type":"Traditional","status":"Available"}}}"""
        val caches = clientWith(json).caches.searchAndRetrieve(
            type = listOf("Traditional"),
            recommendedOnly = true,
            ownerUuid = listOf("some-uuid"),
            minFounds = 10,
            limit = 50,
        )
        assertEquals(1, caches.size)
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
