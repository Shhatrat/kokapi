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
        val json = """{"revision":1962,"fulldump_url":"https://opencaching.pl/okapi/fulldump.tar.gz"}"""
        val info = clientWith(json).replicate.info()
        assertEquals(1962L, info.revision)
        assertEquals("https://opencaching.pl/okapi/fulldump.tar.gz", info.fulldumpUrl)
    }

    @Test
    fun infoWithNullFulDumpUrl() = runTest {
        val json = """{"revision":100}"""
        val info = clientWith(json).replicate.info()
        assertEquals(100L, info.revision)
        assertNull(info.fulldumpUrl)
    }

    @Test
    fun fullDumpReturnsUrl() = runTest {
        val json = """{"url":"https://opencaching.pl/okapi/fulldump.tar.gz"}"""
        val url = clientWith(json).replicate.fullDump()
        assertEquals("https://opencaching.pl/okapi/fulldump.tar.gz", url)
    }

    @Test
    fun changelogReturnsParsedChangelog() = runTest {
        val json = """{"revision":1962,"more":false,"changelog":[{"action":"replace","cache_code":"OP1234","change_type":"other"}]}"""
        val changelog = clientWith(json).replicate.changelog(1900L)
        assertEquals(1962L, changelog.revision)
        assertEquals(false, changelog.more)
        assertEquals(1, changelog.changelog.size)
        assertEquals("replace", changelog.changelog[0].action)
        assertEquals("OP1234", changelog.changelog[0].cacheCode)
    }

    @Test
    fun currentRevisionReturnsLong() = runTest {
        val json = """{"revision":1962,"more":false,"changelog":[]}"""
        val revision = clientWith(json).replicate.currentRevision()
        assertEquals(1962L, revision)
    }

    @Test
    fun changelogEntryHasObjectType() = runTest {
        val json = """{"revision":100,"more":false,"changelog":[
            {"action":"replace","cache_code":"OP1","object_type":"geocache","change_type":"attr_changed"},
            {"action":"replace","log_uuid":"some-uuid","object_type":"log_entry"}
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
            """{"revision":50,"more":true,"changelog":[{"action":"replace","object_type":"geocache","cache_code":"OP1"}]}""",
            """{"revision":100,"more":false,"changelog":[{"action":"replace","object_type":"geocache","cache_code":"OP2"}]}""",
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
