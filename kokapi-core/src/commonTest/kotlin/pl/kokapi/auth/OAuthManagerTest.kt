package pl.kokapi.auth

import pl.kokapi.KokapiSite
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OAuthManagerTest {

    private fun manager(storage: TokenStorage = InMemoryTokenStorage()) = OAuthManager(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "testConsumerKey",
        consumerSecret = "testConsumerSecret",
        callbackUrl = "kokapi://oauth",
        tokenStorage = storage,
    )

    @Test
    fun notAuthenticatedByDefault() {
        assertFalse(manager().isAuthenticated)
    }

    @Test
    fun authHeaderContainsOauthConsumerKey() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/caches/geocache", emptyMap())
        assertTrue(header.contains("oauth_consumer_key=\"testConsumerKey\""), "Header: $header")
    }

    @Test
    fun authHeaderContainsSignature() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/caches/geocache", emptyMap())
        assertTrue(header.contains("oauth_signature="), "Header: $header")
    }

    @Test
    fun authHeaderContainsTimestampAndNonce() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/test", emptyMap())
        assertTrue(header.contains("oauth_timestamp="))
        assertTrue(header.contains("oauth_nonce="))
    }

    @Test
    fun isAuthenticatedAfterTokensSaved() {
        val storage = InMemoryTokenStorage()
        val m = manager(storage)
        storage.save("oauth_token", "tok123")
        storage.save("oauth_token_secret", "sec456")
        assertTrue(m.isAuthenticated)
    }

    @Test
    fun logoutClearsTokens() {
        val storage = InMemoryTokenStorage()
        val m = manager(storage)
        storage.save("oauth_token", "tok")
        storage.save("oauth_token_secret", "sec")
        m.logout()
        assertFalse(m.isAuthenticated)
    }

    @Test
    fun parseCallbackUrlExtractsVerifier() {
        val m = manager()
        m.storeRequestToken("reqToken", "reqSecret")
        val verifier = m.parseCallbackUrl("kokapi://oauth?oauth_token=reqToken&oauth_verifier=abc123")
        assertNotNull(verifier)
        assertTrue(verifier == "abc123")
    }
}
