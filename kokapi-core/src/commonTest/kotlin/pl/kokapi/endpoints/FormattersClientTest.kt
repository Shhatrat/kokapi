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

class FormattersClientTest {

    private fun clientWithString(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    private fun clientWithBytes(bytes: ByteArray, contentType: String = "application/octet-stream") = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(bytes, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, contentType))
        })
    )

    @Test
    fun gpxReturnsXmlString() = runTest {
        val xml = "<gpx></gpx>"
        val result = clientWithString(xml).formatters.gpx(listOf("OP1234"))
        assertEquals(xml, result)
    }

    @Test
    fun gpxWithOptionsDoesNotThrow() = runTest {
        val xml = "<gpx><wpt/></gpx>"
        val result = clientWithString(xml).formatters.gpx(
            cacheCodes = listOf("OP1", "OP2"),
            langpref = "pl",
            nsGround = true,
            nsOx = true,
            images = true,
            trackables = true,
            altWpts = true,
            markFound = true,
        )
        assertEquals(xml, result)
    }

    @Test
    fun garminReturnsByteArray() = runTest {
        val bytes = ByteArray(4) { it.toByte() }
        val result = clientWithBytes(bytes, "application/zip").formatters.garmin(listOf("OP1234"))
        assertTrue(result.contentEquals(bytes))
    }

    @Test
    fun ggzReturnsByteArray() = runTest {
        val bytes = ByteArray(4) { it.toByte() }
        val result = clientWithBytes(bytes, "application/octet-stream").formatters.ggz(listOf("OP1234"))
        assertTrue(result.contentEquals(bytes))
    }

    @Test
    fun mapTileReturnsByteArray() = runTest {
        val bytes = ByteArray(4) { it.toByte() }
        val result = clientWithBytes(bytes, "image/png").formatters.mapTile(z = 12, x = 1234, y = 5678)
        assertTrue(result.contentEquals(bytes))
    }

    @Test
    fun mapTileWithStatusAndTypeDoesNotThrow() = runTest {
        val bytes = ByteArray(8) { it.toByte() }
        val result = clientWithBytes(bytes, "image/png").formatters.mapTile(
            z = 10, x = 100, y = 200,
            status = "Available",
            type = "Traditional",
        )
        assertTrue(result.contentEquals(bytes))
    }
}
