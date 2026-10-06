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

class AttrsClientTest {

    private fun clientWith(body: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun getAttributeReturnsAttribute() = runTest {
        val json = """{"acode":"A1","name":"Dog friendly","names":{"en":"Dog friendly"}}"""
        val attr = clientWith(json).attrs.attribute("A1")
        assertEquals("A1", attr.acode)
        assertEquals("Dog friendly", attr.name)
        assertTrue(attr.isYes)
    }

    @Test
    fun getAttributesListReturnsList() = runTest {
        val json = """[{"acode":"A1","name":"Dogs"},{"acode":"A2-0","name":"No bikes"}]"""
        val attrs = clientWith(json).attrs.attributes()
        assertEquals(2, attrs.size)
    }
}
