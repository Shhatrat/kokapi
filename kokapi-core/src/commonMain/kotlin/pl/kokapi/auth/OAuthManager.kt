package pl.kokapi.auth

import pl.kokapi.KokapiSite

private const val KEY_ACCESS_TOKEN = "oauth_token"
private const val KEY_ACCESS_TOKEN_SECRET = "oauth_token_secret"
private const val KEY_REQUEST_TOKEN = "oauth_request_token"
private const val KEY_REQUEST_TOKEN_SECRET = "oauth_request_token_secret"

class OAuthManager(
    private val site: KokapiSite,
    internal val consumerKey: String,
    internal val consumerSecret: String,
    private val callbackUrl: String,
    private val tokenStorage: TokenStorage,
) {
    val isAuthenticated: Boolean
        get() = tokenStorage.get(KEY_ACCESS_TOKEN) != null &&
                tokenStorage.get(KEY_ACCESS_TOKEN_SECRET) != null

    internal fun accessCredentials(): OAuthCredentials? {
        val token = tokenStorage.get(KEY_ACCESS_TOKEN) ?: return null
        val secret = tokenStorage.get(KEY_ACCESS_TOKEN_SECRET) ?: return null
        return OAuthCredentials(token, secret)
    }

    internal fun storeRequestToken(token: String, secret: String) {
        tokenStorage.save(KEY_REQUEST_TOKEN, token)
        tokenStorage.save(KEY_REQUEST_TOKEN_SECRET, secret)
    }

    internal fun storeAccessToken(token: String, secret: String) {
        tokenStorage.save(KEY_ACCESS_TOKEN, token)
        tokenStorage.save(KEY_ACCESS_TOKEN_SECRET, secret)
        tokenStorage.clear(KEY_REQUEST_TOKEN)
        tokenStorage.clear(KEY_REQUEST_TOKEN_SECRET)
    }

    fun logout() {
        tokenStorage.clearAll()
    }

    fun getRequestTokenUrl(): String = "${site.baseUrl}/oauth/request_token"

    fun getAuthorizeUrl(requestToken: String): String =
        "${site.baseUrl}/oauth/authorize?oauth_token=${requestToken.oauthEncode()}"

    fun getAccessTokenUrl(): String = "${site.baseUrl}/oauth/access_token"

    fun parseCallbackUrl(url: String): String? {
        val query = url.substringAfter("?", "")
        if (query.isEmpty()) return null
        val params = query.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        return params["oauth_verifier"]
    }

    internal fun buildAuthHeader(
        method: String,
        url: String,
        extraParams: Map<String, String> = emptyMap(),
        tokenCredentials: OAuthCredentials? = accessCredentials(),
    ): String {
        val timestamp = (currentTimeMillis() / 1000).toString()
        val nonce = generateNonce()

        val oauthParams = buildMap {
            put("oauth_consumer_key", consumerKey)
            put("oauth_nonce", nonce)
            put("oauth_signature_method", "HMAC-SHA1")
            put("oauth_timestamp", timestamp)
            put("oauth_version", "1.0")
            tokenCredentials?.let { put("oauth_token", it.token) }
        }

        val allParams = (oauthParams + extraParams)
            .entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key.oauthEncode()}=${it.value.oauthEncode()}" }

        val baseUrl = url.substringBefore("?")
        val signatureBase = listOf(
            method.uppercase(),
            baseUrl.oauthEncode(),
            allParams.oauthEncode(),
        ).joinToString("&")

        val signingKey = "${consumerSecret.oauthEncode()}&${(tokenCredentials?.tokenSecret ?: "").oauthEncode()}"
        val signature = hmacSha1Base64(signingKey, signatureBase)

        val headerParams = (oauthParams + ("oauth_signature" to signature))
            .entries
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key.oauthEncode()}=\"${it.value.oauthEncode()}\"" }

        return "OAuth $headerParams"
    }

    internal fun buildOAuthParams(
        method: String,
        url: String,
        extraParams: Map<String, String> = emptyMap(),
        tokenCredentials: OAuthCredentials? = accessCredentials(),
    ): Map<String, String> {
        val timestamp = (currentTimeMillis() / 1000).toString()
        val nonce = generateNonce()

        val oauthParams = buildMap {
            put("oauth_consumer_key", consumerKey)
            put("oauth_nonce", nonce)
            put("oauth_signature_method", "HMAC-SHA1")
            put("oauth_timestamp", timestamp)
            put("oauth_version", "1.0")
            tokenCredentials?.let { put("oauth_token", it.token) }
        }

        val allParams = (oauthParams + extraParams)
            .entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key.oauthEncode()}=${it.value.oauthEncode()}" }

        val baseUrl = url.substringBefore("?")
        val signatureBase = listOf(
            method.uppercase(),
            baseUrl.oauthEncode(),
            allParams.oauthEncode(),
        ).joinToString("&")

        val signingKey = "${consumerSecret.oauthEncode()}&${(tokenCredentials?.tokenSecret ?: "").oauthEncode()}"
        val signature = hmacSha1Base64(signingKey, signatureBase)

        return oauthParams + ("oauth_signature" to signature)
    }
}
