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
        val json = """{"A1":{"acode":"A1","name":"Dogs"},"A2-0":{"acode":"A2-0","name":"No bikes"}}"""
        val attrs = clientWith(json).attrs.attributes(listOf("A1", "A2-0"))
        assertEquals(2, attrs.size)
    }

    @Test
    fun attributeIndexReturnsMap() = runTest {
        val json = """{"A1":{"acode":"A1","name":"Dogs"},"A2":{"acode":"A2","name":"Bikes"}}"""
        val attrs = clientWith(json).attrs.attributeIndex()
        assertEquals(2, attrs.size)
        val acodes = attrs.map { it.acode }.toSet()
        assertTrue(acodes.contains("A1"))
        assertTrue(acodes.contains("A2"))
    }

    @Test
    fun attributeIndexOnlyLocallyUsed() = runTest {
        val json = """{"A1":{"acode":"A1","name":"Dogs"}}"""
        val attrs = clientWith(json).attrs.attributeIndex(onlyLocallyUsed = true)
        assertEquals(1, attrs.size)
        assertEquals("A1", attrs[0].acode)
    }
}
