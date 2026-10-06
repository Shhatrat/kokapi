package pl.kokapi.test

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage

class FakeKokapiClient(
    private val responses: MutableMap<String, String> = mutableMapOf(),
    private val site: KokapiSite = KokapiSite.OPENCACHING_PL,
    private val consumerKey: String = "fake_key",
) {
    fun respondWith(urlPathContains: String, jsonBody: String) {
        responses[urlPathContains] = jsonBody
    }

    fun build(): KokapiClient = KokapiClient(
        site = site,
        consumerKey = consumerKey,
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { request ->
            val matchKey = responses.keys.firstOrNull { request.url.toString().contains(it) }
            val body = matchKey?.let { responses[it] } ?: "{}"
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )
}
