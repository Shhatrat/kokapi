package pl.kokapi.integration

import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests against the real OKAPI.
 * Run with: OKAPI_CONSUMER_KEY=your_key ./gradlew :kokapi-core:desktopTest --tests "*.OkapiIntegrationTest*"
 *
 * Get a key at: https://opencaching.pl/okapi/signup.html
 */
class OkapiIntegrationTest {

    private val consumerKey = getenv("OKAPI_CONSUMER_KEY")
    private val enabled = consumerKey != null

    private fun client() = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = consumerKey ?: "missing",
        tokenStorage = InMemoryTokenStorage(),
    )

    @Test
    fun withoutKeyReturnsApiError() = runTest {
        val noKeyClient = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "invalid_key_xyz",
            tokenStorage = InMemoryTokenStorage(),
        )
        val e = assertFails { noKeyClient.caches.get("OP9D98") }
        assertTrue(
            e is KokapiException.ApiError || e is KokapiException.HttpError || e is KokapiException.NotAuthenticated,
            "Expected KokapiException but got ${e::class}"
        )
    }

    @Test
    fun getCacheById() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        val cache = client().caches.get("OP9D98")
        assertNotNull(cache.code)
        assertTrue(cache.name.isNotEmpty())
        assertTrue(cache.location.lat != 0.0)
        println("Cache: ${cache.code} — ${cache.name} @ ${cache.location.lat}, ${cache.location.lon}")
    }

    @Test
    fun searchCachesNearKrakow() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        val caches = client().caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 5.0, limit = 5)
        assertTrue(caches.isNotEmpty(), "Expected caches near Krakow")
        caches.forEach { cache ->
            println("  ${cache.code}: ${cache.name} [${cache.type}] D${cache.difficulty}/T${cache.terrain}")
        }
    }

    @Test
    fun getAttributesList() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        val attrs = client().attrs.attributes(listOf("A1", "A2", "A3", "A4", "A5"))
        assertTrue(attrs.isNotEmpty(), "Expected attributes list")
        println("Attributes count: ${attrs.size}")
        attrs.take(3).forEach { println("  ${it.acode}: ${it.name} isYes=${it.isYes}") }
    }

    @Test
    fun getSingleAttribute() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        val attr = client().attrs.attribute("A1")
        assertNotNull(attr.name)
        println("Attribute A1: ${attr.name}")
    }

    @Test
    fun getUserByUsername() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        // Get cache OP9D98 first to discover a valid username
        val cache = client().caches.get("OP9D98")
        val user = client().users.byUsername(cache.owner.username)
        assertNotNull(user.uuid)
        println("User: ${user.username} (${user.cachesFound} finds)")
    }

    @Test
    fun searchBbox() = runTest {
        if (!enabled) { println("SKIP: set OKAPI_CONSUMER_KEY to run"); return@runTest }
        val caches = client().caches.bbox(
            southLat = 49.9, westLon = 19.8,
            northLat = 50.2, eastLon = 20.1,
            limit = 5
        )
        assertTrue(caches.isNotEmpty())
        println("BBox search found ${caches.size} caches")
    }
}

internal expect fun getenv(name: String): String?
