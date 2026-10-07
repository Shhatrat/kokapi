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

class CacheSearchClientTest {

    private fun clientWith(responseBody: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(responseBody, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    private fun twoStepClient(searchJson: String, cachesJson: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { request ->
            val body = if (request.url.encodedPath.contains("search")) searchJson else cachesJson
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    private val searchCodesJson = """{"results":["OP1","OP2"],"more":false}"""
    private val twoCachesJson = """{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}},"OP2":{"code":"OP2","name":"C2","location":"51.1|20.1","type":"Multi","status":"Available","difficulty":2.0,"terrain":2.0,"owner":{"uuid":"u","username":"u"}}}"""
    private val oneCacheJson = """{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}}}"""

    @Test
    fun nearestReturnsList() = runTest {
        val results = twoStepClient(searchCodesJson, twoCachesJson).search.nearest(lat = 51.0, lon = 20.0, radiusKm = 5.0)
        assertEquals(2, results.size)
    }

    @Test
    fun nearestReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").search.nearest(51.0, 20.0)
        assertEquals(0, results.size)
    }

    @Test
    fun nearestWithFilters() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").search.nearest(
            lat = 51.0, lon = 20.0,
            filters = SearchFilters(
                ownerUuid = listOf("uuid-owner"),
                foundBy = "uuid-found",
                recommendedOnly = true,
                excludeMyOwn = true,
                watchedOnly = true,
                ftfHunter = true,
                rating = "3-5",
            )
        )
        assertEquals(0, results.size)
    }

    @Test
    fun bboxReturnsList() = runTest {
        val results = twoStepClient(searchCodesJson, twoCachesJson).search.bbox(
            southLat = 50.0, westLon = 19.0, northLat = 51.0, eastLon = 21.0
        )
        assertEquals(2, results.size)
    }

    @Test
    fun bboxWithFilters() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").search.bbox(
            southLat = 50.0, westLon = 19.0, northLat = 51.0, eastLon = 21.0,
            filters = SearchFilters(foundBy = "uuid", recommendedOnly = true)
        )
        assertEquals(0, results.size)
    }

    @Test
    fun allReturnsList() = runTest {
        val results = twoStepClient("""{"results":["OP1"],"more":false}""", oneCacheJson).search.all()
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
    }

    @Test
    fun allWithFilters() = runTest {
        val results = clientWith("""{"results":[],"more":false}""").search.all(
            SearchFilters(
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
        )
        assertEquals(0, results.size)
    }

    @Test
    fun byUrlsReturnsList() = runTest {
        val client = twoStepClient("""{"results":["OP1"],"more":false}""", oneCacheJson)
        val results = client.search.byUrls(listOf("https://opencaching.pl/viewcache.php?wp=OP1"))
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
    }

    @Test
    fun byUrlsReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":[],"more":false}""")
            .search.byUrls(listOf("https://opencaching.pl/viewcache.php?wp=OPXXX"))
        assertEquals(0, results.size)
    }

    @Test
    fun saveReturnsSetId() = runTest {
        val setId = clientWith("""{"set_id":"abc123"}""").search.save()
        assertEquals("abc123", setId)
    }

    @Test
    fun saveWithFiltersReturnsSetId() = runTest {
        val setId = clientWith("""{"set_id":"xyz789"}""")
            .search.save(SearchFilters(type = listOf("Traditional"), limit = 50))
        assertEquals("xyz789", setId)
    }

    @Test
    fun saveWithFiltersReturnsSetId2() = runTest {
        val setId = clientWith("""{"set_id":"xyz"}""").search.save(
            SearchFilters(foundBy = "uuid-found", recommendedOnly = true, ftfHunter = true)
        )
        assertEquals("xyz", setId)
    }

    @Test
    fun andRetrieveReturnsList() = runTest {
        val json = """{"results":{"OP1":{"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"}}}}"""
        val results = clientWith(json).search.andRetrieve()
        assertEquals(1, results.size)
        assertEquals("OP1", results[0].code)
    }

    @Test
    fun andRetrieveReturnsEmptyList() = runTest {
        val results = clientWith("""{"results":{}}""").search.andRetrieve()
        assertEquals(0, results.size)
    }

    @Test
    fun andRetrieveWithFilters() = runTest {
        val json = """{"results":{"OP1":{"code":"OP1","name":"T","location":"52.0|21.0","type":"Traditional","status":"Available"}}}"""
        val caches = clientWith(json).search.andRetrieve(
            SearchFilters(type = listOf("Traditional"), recommendedOnly = true, ownerUuid = listOf("some-uuid"), minFounds = 10, limit = 50)
        )
        assertEquals(1, caches.size)
    }

    @Test
    fun flowEmitsAllCaches() = runTest {
        var callCount = 0
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
        client.search.flow(52.0, 21.0, pageSize = 2).collect { collected.add(it) }
        assertEquals(2, collected.size)
        assertEquals("OP1", collected[0].code)
        assertEquals("OP2", collected[1].code)
    }
}
