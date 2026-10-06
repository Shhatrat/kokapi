package pl.kokapi.auth

internal data class OAuthCredentials(
    val token: String,
    val tokenSecret: String,
)
