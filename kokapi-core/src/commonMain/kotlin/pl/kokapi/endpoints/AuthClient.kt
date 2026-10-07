package pl.kokapi.endpoints

import pl.kokapi.auth.OAuthManager
import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine

/**
 * Intermediate result of the first step of the OAuth 1.0a login flow.
 *
 * Pass [authorizeUrl] to the user's browser; after the user authorises the app,
 * call [AuthClient.completeLogin] with the callback URL or PIN.
 */
data class OAuthLoginStep(
    /** URL to open in a browser so the user can authorise the application. */
    val authorizeUrl: String,
    internal val requestToken: String,
    internal val requestTokenSecret: String,
)

/**
 * Client for the two-step OAuth 1.0a authentication flow.
 *
 * **Step 1 — [startLogin]:** Exchanges the consumer key/secret for a temporary request token
 * and returns [OAuthLoginStep.authorizeUrl]. Open this URL in a browser so the user can grant access.
 *
 * **Step 2 — [completeLogin]:** After the user approves, the site redirects to your [callbackUrl]
 * (or shows a PIN for out-of-band flows). Pass the full callback URL (or the PIN) to
 * [completeLogin]; it exchanges the verifier for a persistent access token.
 *
 * Tokens are persisted via the [TokenStorage][pl.kokapi.auth.TokenStorage] provided to [pl.kokapi.KokapiClient].
 */
class AuthClient internal constructor(
    private val oauthManager: OAuthManager,
    private val engine: HttpEngine,
) {
    /** `true` if a valid access token is currently stored. */
    val isAuthenticated: Boolean get() = oauthManager.isAuthenticated

    /**
     * Step 1: Obtains a request token from the OKAPI server and builds the authorisation URL.
     *
     * @return [OAuthLoginStep] containing the URL to open in a browser.
     */
    suspend fun startLogin(): OAuthLoginStep {
        val response = engine.post("oauth/request_token", mapOf("oauth_callback" to "oob"), forceConsumerAuth = true)
        val params = response.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        val token = params["oauth_token"]
            ?: throw KokapiException.ApiError("oauth_token_missing", "No oauth_token in response: $response")
        val secret = params["oauth_token_secret"]
            ?: throw KokapiException.ApiError("oauth_token_secret_missing", "No oauth_token_secret in response")
        oauthManager.storeRequestToken(token, secret)
        return OAuthLoginStep(
            authorizeUrl = oauthManager.getAuthorizeUrl(token),
            requestToken = token,
            requestTokenSecret = secret,
        )
    }

    /**
     * Step 2: Exchanges the OAuth verifier for an access token and persists it.
     *
     * @param callbackUrl The full redirect URL received after user authorisation,
     *   or just the PIN string for out-of-band flows.
     * @return `true` if the access token was successfully obtained and stored.
     */
    suspend fun completeLogin(callbackUrl: String): Boolean {
        val verifier = oauthManager.parseCallbackUrl(callbackUrl) ?: return false
        val response = engine.post("oauth/access_token", mapOf("oauth_verifier" to verifier), forceConsumerAuth = true)
        val params = response.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        val token = params["oauth_token"] ?: return false
        val secret = params["oauth_token_secret"] ?: return false
        oauthManager.storeAccessToken(token, secret)
        return true
    }

    /** Clears the stored access token, effectively logging the user out. */
    fun logout() = oauthManager.logout()
}
