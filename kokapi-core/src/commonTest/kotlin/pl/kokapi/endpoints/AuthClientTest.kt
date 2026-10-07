package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "test_consumer_key",
        consumerSecret = "test_consumer_secret",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "text/plain"))
        })
    )

    // ── startLogin ────────────────────────────────────────────────────────────

    @Test
    fun startLoginReturnsAuthorizeUrl() = runTest {
        val client = clientWith("oauth_token=req_token&oauth_token_secret=req_secret&oauth_callback_confirmed=true")
        val step = client.auth.startLogin()
        assertNotNull(step.authorizeUrl)
        assertTrue(step.authorizeUrl.contains("req_token"), "authorizeUrl should contain the token")
    }

    @Test
    fun startLoginThrowsOnMissingToken() = runTest {
        val client = clientWith("oauth_token_secret=req_secret")
        val ex = assertFailsWith<KokapiException.ApiError> {
            client.auth.startLogin()
        }
        assertEquals("oauth_token_missing", ex.errorCode)
    }

    @Test
    fun startLoginThrowsOnMissingSecret() = runTest {
        val client = clientWith("oauth_token=req_token")
        val ex = assertFailsWith<KokapiException.ApiError> {
            client.auth.startLogin()
        }
        assertEquals("oauth_token_secret_missing", ex.errorCode)
    }

    @Test
    fun startLoginThrowsOnEmptyResponse() = runTest {
        val client = clientWith("")
        assertFailsWith<KokapiException.ApiError> {
            client.auth.startLogin()
        }
    }

    // ── completeLogin ─────────────────────────────────────────────────────────

    @Test
    fun completeLoginReturnsTrueOnSuccess() = runTest {
        val client = clientWith("oauth_token=access_token&oauth_token_secret=access_secret")
        val result = client.auth.completeLogin("https://example.com/callback?oauth_verifier=verifier123")
        assertTrue(result)
    }

    @Test
    fun completeLoginReturnsFalseWhenCallbackHasNoVerifier() = runTest {
        val client = clientWith("oauth_token=access_token&oauth_token_secret=access_secret")
        val result = client.auth.completeLogin("https://example.com/callback?no_verifier=here")
        assertFalse(result)
    }

    @Test
    fun completeLoginReturnsFalseOnMissingAccessToken() = runTest {
        val client = clientWith("oauth_token_secret=access_secret")
        val result = client.auth.completeLogin("https://example.com/callback?oauth_verifier=v123")
        assertFalse(result)
    }

    @Test
    fun completeLoginReturnsFalseOnMissingAccessSecret() = runTest {
        val client = clientWith("oauth_token=access_token")
        val result = client.auth.completeLogin("https://example.com/callback?oauth_verifier=v123")
        assertFalse(result)
    }

    @Test
    fun completeLoginReturnsFalseForPinWithNoVerifier() = runTest {
        val client = clientWith("oauth_token=access_token&oauth_token_secret=access_secret")
        // PIN string has no "?" so parseCallbackUrl returns null
        val result = client.auth.completeLogin("123456")
        assertFalse(result)
    }

    // ── isAuthenticated ───────────────────────────────────────────────────────

    @Test
    fun isAuthenticatedFalseInitially() = runTest {
        val client = clientWith("")
        assertFalse(client.auth.isAuthenticated)
    }

    @Test
    fun isAuthenticatedTrueAfterCompleteLogin() = runTest {
        val client = clientWith("oauth_token=access_token&oauth_token_secret=access_secret")
        client.auth.completeLogin("https://example.com/callback?oauth_verifier=v123")
        assertTrue(client.auth.isAuthenticated)
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun logoutClearsAuthentication() = runTest {
        val client = clientWith("oauth_token=access_token&oauth_token_secret=access_secret")
        client.auth.completeLogin("https://example.com/callback?oauth_verifier=v123")
        assertTrue(client.auth.isAuthenticated)
        client.auth.logout()
        assertFalse(client.auth.isAuthenticated)
    }

    @Test
    fun logoutOnUnauthenticatedClientDoesNotThrow() {
        val client = clientWith("")
        client.auth.logout()
        assertFalse(client.auth.isAuthenticated)
    }
}
