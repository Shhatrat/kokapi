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
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ApiRefClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun methodReturnsParsedApiMethod() = runTest {
        val json = """{"name":"services/caches/geocache","brief_description":"Returns info about a geocache.","arguments":[{"name":"cache_code","is_required":true},{"name":"fields","is_required":false},{"name":"langpref","is_required":false}]}"""
        val result = clientWith(json).apiRef.method("services/caches/geocache")
        assertEquals("services/caches/geocache", result.methodPath)
        assertEquals("Returns info about a geocache.", result.description)
        assertTrue(result.requiredFields.contains("cache_code"))
        assertTrue(result.availableFields.contains("fields"))
        assertTrue(result.availableFields.contains("cache_code"))
    }

    @Test
    fun methodWithNoFieldsReturnsEmptyLists() = runTest {
        val json = """{"name":"services/apisrv/stats","brief_description":"Returns stats.","arguments":[]}"""
        val result = clientWith(json).apiRef.method("services/apisrv/stats")
        assertEquals("services/apisrv/stats", result.methodPath)
        assertTrue(result.requiredFields.isEmpty())
        assertTrue(result.availableFields.isEmpty())
    }

    @Test
    fun methodIndexReturnsListOfSummaries() = runTest {
        val json = """[{"name":"services/caches/geocache","brief_description":"Returns geocache info."},{"name":"services/apisrv/stats","brief_description":"Returns stats."}]"""
        val result = clientWith(json).apiRef.methodIndex()
        assertEquals(2, result.size)
        assertEquals("services/caches/geocache", result[0].methodPath)
        assertEquals("Returns geocache info.", result[0].description)
        assertEquals("services/apisrv/stats", result[1].methodPath)
    }

    @Test
    fun methodIndexReturnsEmptyListForEmptyArray() = runTest {
        val result = clientWith("[]").apiRef.methodIndex()
        assertEquals(0, result.size)
    }

    @Test
    fun reportIssueReturnsTrueOnSuccess() = runTest {
        val result = clientWith("""{"success":true}""").apiRef.reportIssue("services/caches/geocache", "Missing field X")
        assertTrue(result)
    }

    @Test
    fun reportIssueReturnsFalseOnFailure() = runTest {
        val result = clientWith("""{"success":false}""").apiRef.reportIssue("services/caches/geocache", "Issue description")
        assertFalse(result)
    }
}
