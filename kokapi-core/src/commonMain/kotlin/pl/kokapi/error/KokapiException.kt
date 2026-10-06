package pl.kokapi.error

sealed class KokapiException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {

    data class ApiError(val errorCode: String, override val message: String) :
        KokapiException(message)

    data class HttpError(val code: Int, val reason: String) :
        KokapiException("HTTP $code: $reason")

    object NotAuthenticated :
        KokapiException("Not authenticated. Call kokapi.auth.startLogin() first.")

    object NetworkError :
        KokapiException("Network unavailable.")

    data class Unknown(override val cause: Throwable?) :
        KokapiException("Unexpected error", cause)
}
