package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.LogType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LogsClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun submitLogSucceeds() = runTest {
        val json = """{"success":true,"message":"Saved","log_uuid":"abc-123"}"""
        val uuid = clientWith(json).logs.submit("OP1234", LogType.FOUND, "Great cache!")
        assertEquals("abc-123", uuid)
    }

    @Test
    fun submitLogThrowsOnFailureResponse() = runTest {
        val json = """{"success":false,"message":"Not allowed","log_uuid":null}"""
        assertFailsWith<KokapiException.ApiError> {
            clientWith(json).logs.submit("OP1234", LogType.FOUND)
        }
    }

    @Test
    fun getLogReturnsGeocacheLog() = runTest {
        val json = """
            {"uuid":"log-1","cache_code":"OP1234","user":{"uuid":"u1","username":"tester"},
             "type":"Found it","date":"2026-10-06","comment":"Found it!"}
        """.trimIndent()
        val log = clientWith(json).logs.get("log-1")
        assertEquals("log-1", log.uuid)
        assertEquals(LogType.FOUND, log.type)
    }
}
