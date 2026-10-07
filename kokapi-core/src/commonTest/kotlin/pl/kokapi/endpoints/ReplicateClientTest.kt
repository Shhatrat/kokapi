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
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ReplicateClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun infoReturnsParsedModel() = runTest {
        val json = """{"changelog":{"revision":1962,"min_since":1000},"latest_fulldump":{"revision":1950,"generated_at":"2026-01-01","size":1000}}"""
        val info = clientWith(json).replicate.info()
        assertEquals(1962L, info.revision)
        assertEquals(1000L, info.minSince)
        assertEquals(1950L, info.latestFulldumpRevision)
    }

    @Test
    fun infoWithNullFulldump() = runTest {
        val json = """{"changelog":{"revision":100,"min_since":50}}"""
        val info = clientWith(json).replicate.info()
        assertEquals(100L, info.revision)
        assertNull(info.latestFulldumpRevision)
    }

    @Test
    fun fullDumpReturnsUrl() = runTest {
        val json = """{"url":"https://opencaching.pl/okapi/fulldump.tar.gz"}"""
        val url = clientWith(json).replicate.fullDump()
        assertEquals("https://opencaching.pl/okapi/fulldump.tar.gz", url)
    }

    @Test
    fun changelogReturnsParsedChangelog() = runTest {
        val json = """{"revision":1962,"more":false,"changelog":[{"object_type":"geocache","object_key":{"code":"OP1234"},"change_type":"replace"}]}"""
        val changelog = clientWith(json).replicate.changelog(1900L)
        assertEquals(1962L, changelog.revision)
        assertEquals(false, changelog.more)
        assertEquals(1, changelog.changelog.size)
        assertEquals("replace", changelog.changelog[0].changeType)
        assertEquals("OP1234", changelog.changelog[0].cacheCode)
    }

    @Test
    fun currentRevisionReturnsLong() = runTest {
        val json = """{"changelog":{"revision":1962,"min_since":1000}}"""
        val revision = clientWith(json).replicate.currentRevision()
        assertEquals(1962L, revision)
    }

    @Test
    fun changelogEntryHasObjectType() = runTest {
        val json = """{"revision":100,"more":false,"changelog":[
            {"object_type":"geocache","object_key":{"code":"OP1"},"change_type":"replace"},
            {"object_type":"log_entry","object_key":{"uuid":"some-uuid"},"change_type":"replace"}
        ]}"""
        val result = clientWith(json).replicate.changelog(0L)
        assertEquals(ChangelogObjectType.GEOCACHE, result.changelog[0].objectType)
        assertEquals("OP1", result.changelog[0].cacheCode)
        assertEquals(ChangelogObjectType.LOG_ENTRY, result.changelog[1].objectType)
        assertEquals("some-uuid", result.changelog[1].logUuid)
    }

    @Test
    fun changelogAllFetchesAllPages() = runTest {
        var callCount = 0
        val responses = listOf(
            """{"revision":50,"more":true,"changelog":[{"object_type":"geocache","object_key":{"code":"OP1"},"change_type":"replace"}]}""",
            """{"revision":100,"more":false,"changelog":[{"object_type":"geocache","object_key":{"code":"OP2"},"change_type":"replace"}]}""",
        )
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine {
                respond(responses[callCount++], HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )
        val result = client.replicate.changelogAll(0L)
        assertEquals(2, result.changelog.size)
        assertEquals(100L, result.revision)
        assertFalse(result.more)
    }
}
