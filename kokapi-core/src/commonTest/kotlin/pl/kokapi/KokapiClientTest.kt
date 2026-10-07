package pl.kokapi

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import pl.kokapi.auth.InMemoryTokenStorage
import kotlin.test.Test
import kotlin.test.assertNotNull

class KokapiClientTest {

    @Test
    fun clientExposesAllEndpointGroups() {
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "key",
            consumerSecret = "secret",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        assertNotNull(client.caches)
        assertNotNull(client.search)
        assertNotNull(client.logs)
        assertNotNull(client.users)
        assertNotNull(client.attrs)
        assertNotNull(client.replicate)
        assertNotNull(client.auth)
    }
}
