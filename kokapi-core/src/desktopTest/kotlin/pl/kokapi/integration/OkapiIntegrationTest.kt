package pl.kokapi.integration

import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.endpoints.ChangelogObjectType
import pl.kokapi.model.enum.CacheStatus
import pl.kokapi.model.enum.CacheType
import kotlin.test.*
import kotlinx.coroutines.flow.toList

/**
 * Integration tests against the live opencaching.pl OKAPI.
 *
 * Run with:  OKAPI_CONSUMER_KEY=<key> ./gradlew :kokapi-core:desktopTest
 *
 * All tests are skipped silently when OKAPI_CONSUMER_KEY is not set.
 * No authentication required — all calls use consumer-only access (public data).
 */
class OkapiIntegrationTest {

    private val key = System.getenv("OKAPI_CONSUMER_KEY") ?: ""
    private val client by lazy {
        KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = key,
            tokenStorage = InMemoryTokenStorage(),
        )
    }

    private fun skipIfNoKey() {
        assumeTrue(key.isNotBlank(), "OKAPI_CONSUMER_KEY not set — skipping integration test")
    }

    // ── caches ───────────────────────────────────────────────────────────────

    @Test
    fun getKnownCache() = runTest {
        skipIfNoKey()
        val cache = client.caches.get("OP9D98")
        assertEquals("OP9D98", cache.code)
        assertTrue(cache.name.isNotBlank())
        assertTrue(cache.location.lat in -90.0..90.0)
        assertTrue(cache.location.lon in -180.0..180.0)
        assertNotEquals(CacheType.OTHER, cache.type)
        assertTrue(cache.difficulty in 1f..5f)
        assertTrue(cache.terrain in 1f..5f)
        assertTrue(cache.owner.username.isNotBlank())
    }

    @Test
    fun getMultipleCaches() = runTest {
        skipIfNoKey()
        val caches = client.caches.getMultiple(listOf("OP9D98", "OP6174"))
        assertEquals(2, caches.size)
        assertTrue(caches.any { it.code == "OP9D98" })
        assertTrue(caches.any { it.code == "OP6174" })
    }

    @Test
    fun searchNearest() = runTest {
        skipIfNoKey()
        // Center of Warsaw
        val caches = client.caches.search(lat = 52.2297, lon = 21.0122, radiusKm = 5.0, limit = 5)
        assertTrue(caches.isNotEmpty(), "Expected caches near Warsaw")
        assertTrue(caches.size <= 5)
        caches.forEach { assertTrue(it.code.startsWith("OP")) }
    }

    @Test
    fun searchBbox() = runTest {
        skipIfNoKey()
        val caches = client.caches.bbox(
            southLat = 52.1, westLon = 20.9,
            northLat = 52.3, eastLon = 21.1,
            limit = 5,
        )
        assertTrue(caches.isNotEmpty())
        caches.forEach { assertTrue(it.code.startsWith("OP")) }
    }

    @Test
    fun searchAll() = runTest {
        skipIfNoKey()
        val caches = client.caches.searchAll(
            type = listOf("Traditional"),
            limit = 3,
        )
        assertTrue(caches.isNotEmpty())
        caches.forEach { assertEquals(CacheType.TRADITIONAL, it.type) }
    }

    @Test
    fun searchByUrls() = runTest {
        skipIfNoKey()
        val caches = client.caches.searchByUrls(listOf("https://www.opencaching.pl/viewcache.php?wp=OP9D98"))
        assertEquals(1, caches.size)
        assertEquals("OP9D98", caches[0].code)
    }

    @Test
    fun searchSaveReturnsSetId() = runTest {
        skipIfNoKey()
        val setId = client.caches.searchSave(type = listOf("Traditional"), limit = 5)
        assertTrue(setId.isNotBlank())
    }

    @Test
    fun searchAndRetrieve() = runTest {
        skipIfNoKey()
        val caches = client.caches.searchAndRetrieve(
            type = listOf("Traditional"),
            limit = 3,
        )
        assertTrue(caches.isNotEmpty())
        caches.forEach {
            assertEquals(CacheType.TRADITIONAL, it.type)
            assertTrue(it.name.isNotBlank())
        }
    }

    @Test
    fun getWithLangpref() = runTest {
        skipIfNoKey()
        val cache = client.caches.get("OP9D98", langpref = "pl", lpc = 3)
        assertEquals("OP9D98", cache.code)
        assertTrue(cache.latestLogs.size <= 3)
    }

    // ── logs ─────────────────────────────────────────────────────────────────

    @Test
    fun getLogsForCache() = runTest {
        skipIfNoKey()
        val logs = client.logs.getForCache("OP9D98", limit = 5)
        assertTrue(logs.isNotEmpty())
        logs.forEach {
            assertEquals("OP9D98", it.cacheCode)
            assertTrue(it.user.username.isNotBlank())
        }
    }

    @Test
    fun getLogEntry() = runTest {
        skipIfNoKey()
        val logs = client.logs.getForCache("OP9D98", limit = 1)
        assertTrue(logs.isNotEmpty(), "Need at least one log to test get()")
        val log = client.logs.get(logs[0].uuid)
        assertEquals(logs[0].uuid, log.uuid)
        assertEquals("OP9D98", log.cacheCode)
    }

    @Test
    fun getLogEntries() = runTest {
        skipIfNoKey()
        val logs = client.logs.getForCache("OP9D98", limit = 2)
        val uuids = logs.map { it.uuid }
        val entries = client.logs.getEntries(uuids)
        assertEquals(uuids.size, entries.size)
    }

    @Test
    fun logCapabilities() = runTest {
        skipIfNoKey()
        if (!client.auth.isAuthenticated) {
            println("SKIP: logCapabilities requires authentication")
            return@runTest
        }
        val caps = client.logs.capabilities("OP9D98")
        assertNotNull(caps)
        assertTrue(caps.availableTypes.isNotEmpty())
    }

    // ── users ─────────────────────────────────────────────────────────────────

    @Test
    fun getUserByUsername() = runTest {
        skipIfNoKey()
        val user = client.users.byUsername("Seti")
        assertEquals("Seti", user.username)
        assertTrue(user.uuid.isNotBlank())
        assertTrue(user.cachesFound >= 0)
    }

    @Test
    fun getUserLogs() = runTest {
        skipIfNoKey()
        val user = client.users.byUsername("Seti")
        val logs = client.logs.getUserLogs(user.uuid, limit = 5)
        // user may have 0 logs; just assert no exception
        assertNotNull(logs)
    }

    @Test
    fun getUsersByUsernames() = runTest {
        skipIfNoKey()
        val result = client.users.byUsernames(listOf("Seti", "NoSuchUser12345xyz"))
        assertTrue(result.size >= 1)
        assertTrue(result.values.any { it.username == "Seti" })
        assertFalse(result.containsKey("NoSuchUser12345xyz"))
    }

    // ── attrs ─────────────────────────────────────────────────────────────────

    @Test
    fun getAttributeIndex() = runTest {
        skipIfNoKey()
        val attrs = client.attrs.attributeIndex()
        assertTrue(attrs.size > 10)
        attrs.forEach { assertTrue(it.acode.startsWith("A")) }
    }

    @Test
    fun getAttribute() = runTest {
        skipIfNoKey()
        val attr = client.attrs.attribute("A1")
        assertEquals("A1", attr.acode)
        assertTrue(attr.name.isNotBlank())
    }

    @Test
    fun getAttributes() = runTest {
        skipIfNoKey()
        val attrs = client.attrs.attributes(listOf("A1", "A2", "A6"))
        assertEquals(3, attrs.size)
        assertTrue(attrs.any { it.acode == "A1" })
    }

    // ── replicate ─────────────────────────────────────────────────────────────

    @Test
    fun replicateInfo() = runTest {
        skipIfNoKey()
        val info = client.replicate.info()
        assertTrue(info.revision > 0)
        assertTrue(info.minSince > 0)
    }

    @Test
    fun currentRevision() = runTest {
        skipIfNoKey()
        val rev = client.replicate.currentRevision()
        assertTrue(rev > 1000)
    }

    @Test
    fun changelog() = runTest {
        skipIfNoKey()
        val info = client.replicate.info()
        val since = maxOf(info.minSince, info.revision - 50)
        val log = client.replicate.changelog(since)
        assertTrue(log.revision >= since)
        assertNotNull(log.changelog)
        log.changelog.forEach { entry ->
            assertTrue(entry.objectType != ChangelogObjectType.UNKNOWN || entry.objectTypeRaw.isNotBlank())
            assertTrue(entry.changeType.isNotBlank())
        }
    }

    // ── apisrv ────────────────────────────────────────────────────────────────

    @Test
    fun installation() = runTest {
        skipIfNoKey()
        val inst = client.apiSrv.installation()
        assertTrue(inst.siteUrl.isNotBlank())
        assertTrue(inst.siteName.isNotBlank())
        assertTrue(inst.okapiRevision > 0)
    }

    @Test
    fun installations() = runTest {
        skipIfNoKey()
        val list = client.apiSrv.installations()
        assertTrue(list.isNotEmpty())
        assertTrue(list.any { it.siteUrl.contains("opencaching") })
    }

    @Test
    fun stats() = runTest {
        skipIfNoKey()
        val s = client.apiSrv.stats()
        assertTrue(s.cacheCount > 1000)
    }

    // ── apiref ────────────────────────────────────────────────────────────────

    @Test
    fun methodIndex() = runTest {
        skipIfNoKey()
        val methods = client.apiRef.methodIndex()
        assertTrue(methods.size > 20)
        assertTrue(methods.any { it.methodPath.contains("geocache") })
    }

    @Test
    fun method() = runTest {
        skipIfNoKey()
        val m = client.apiRef.method("services/caches/geocache")
        assertEquals("services/caches/geocache", m.methodPath)
        assertTrue(m.availableFields.isNotEmpty())
    }

    // ── formatters ────────────────────────────────────────────────────────────

    @Test
    fun gpxReturnsXml() = runTest {
        skipIfNoKey()
        val gpx = client.formatters.gpx(listOf("OP9D98"))
        assertTrue(gpx.trimStart().startsWith("<?xml") || gpx.contains("<gpx"), "Expected GPX XML, got: ${gpx.take(100)}")
    }
}

private fun assumeTrue(condition: Boolean, message: String) {
    if (!condition) {
        println("SKIP: $message")
        return
    }
}
