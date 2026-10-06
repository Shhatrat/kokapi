package pl.kokapi.endpoints

import pl.kokapi.auth.OAuthManager
import pl.kokapi.http.HttpEngine

data class OAuthLoginStep(
    val authorizeUrl: String,
    internal val requestToken: String,
    internal val requestTokenSecret: String,
)

class AuthClient internal constructor(
    private val oauthManager: OAuthManager,
    private val engine: HttpEngine,
) {
    val isAuthenticated: Boolean get() = oauthManager.isAuthenticated

    suspend fun startLogin(): OAuthLoginStep {
        val response = engine.post("oauth/request_token", mapOf("oauth_callback" to "oob"))
        val params = response.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        val token = params["oauth_token"] ?: error("No oauth_token in response: $response")
        val secret = params["oauth_token_secret"] ?: error("No oauth_token_secret in response")
        oauthManager.storeRequestToken(token, secret)
        return OAuthLoginStep(
            authorizeUrl = oauthManager.getAuthorizeUrl(token),
            requestToken = token,
            requestTokenSecret = secret,
        )
    }

    suspend fun completeLogin(callbackUrl: String): Boolean {
        val verifier = oauthManager.parseCallbackUrl(callbackUrl) ?: return false
        val response = engine.post("oauth/access_token", mapOf("oauth_verifier" to verifier))
        val params = response.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        val token = params["oauth_token"] ?: return false
        val secret = params["oauth_token_secret"] ?: return false
        oauthManager.storeAccessToken(token, secret)
        return true
    }

    fun logout() = oauthManager.logout()
}
