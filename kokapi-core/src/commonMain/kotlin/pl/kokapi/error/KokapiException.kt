package pl.kokapi.error

/**
 * Sealed exception hierarchy for all errors thrown by KoKAPI.
 *
 * Catch the specific subclass you care about, or catch [KokapiException] to handle all library errors.
 */
sealed class KokapiException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {

    /**
     * The OKAPI server returned a structured error response.
     *
     * @property errorCode Machine-readable error code from the OKAPI response.
     */
    data class ApiError(val errorCode: String, override val message: String) :
        KokapiException(message)

    /**
     * The server returned an unexpected HTTP status code.
     *
     * @property code HTTP status code (e.g. 404, 500).
     * @property reason Human-readable reason phrase.
     */
    data class HttpError(val code: Int, val reason: String) :
        KokapiException("HTTP $code: $reason")

    /** An authenticated endpoint was called without a stored access token. */
    object NotAuthenticated :
        KokapiException("Not authenticated. Call kokapi.auth.startLogin() first.")

    /** The request could not be completed due to a network connectivity problem. */
    object NetworkError :
        KokapiException("Network unavailable.")

    /**
     * An unexpected error that does not fit any other category.
     *
     * @property cause The underlying throwable that triggered this exception.
     */
    data class Unknown(override val cause: Throwable?) :
        KokapiException("Unexpected error", cause)
}
