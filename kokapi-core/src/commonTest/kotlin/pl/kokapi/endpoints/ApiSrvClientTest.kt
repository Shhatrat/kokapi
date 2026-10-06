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

class ApiSrvClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun installationReturnsParsedModel() = runTest {
        val json = """{"site_url":"https://opencaching.pl","okapi_base_url":"https://opencaching.pl/okapi","site_name":"Opencaching.PL","okapi_revision":1962,"registration_url":"https://opencaching.pl/okapi/signup.html"}"""
        val installation = clientWith(json).apiSrv.installation()
        assertEquals("Opencaching.PL", installation.siteName)
        assertEquals("https://opencaching.pl", installation.siteUrl)
        assertEquals("https://opencaching.pl/okapi", installation.okapiBaseUrl)
        assertEquals(1962, installation.okapiRevision)
        assertEquals("https://opencaching.pl/okapi/signup.html", installation.registrationUrl)
    }

    @Test
    fun statsReturnsParsedModel() = runTest {
        val json = """{"cache_count":42000}"""
        val stats = clientWith(json).apiSrv.stats()
        assertEquals(42000, stats.cacheCount)
    }

    @Test
    fun installationsReturnsListOfInstallations() = runTest {
        val json = """[{"site_url":"https://opencaching.pl","okapi_base_url":"https://opencaching.pl/okapi","site_name":"Opencaching.PL","okapi_revision":1962}]"""
        val installations = clientWith(json).apiSrv.installations()
        assertEquals(1, installations.size)
        assertEquals("Opencaching.PL", installations[0].siteName)
        assertEquals(1962, installations[0].okapiRevision)
    }

    @Test
    fun installationsReturnsEmptyListForEmptyArray() = runTest {
        val installations = clientWith("[]").apiSrv.installations()
        assertEquals(0, installations.size)
    }
}
