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
}
