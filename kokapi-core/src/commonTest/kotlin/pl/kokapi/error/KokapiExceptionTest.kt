package pl.kokapi.error

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

class KokapiExceptionTest {

    @Test
    fun apiErrorContainsCode() {
        val e = KokapiException.ApiError("invalid_oauth_request", "Bad signature")
        assertEquals("invalid_oauth_request", e.errorCode)
        assertEquals("Bad signature", e.message)
    }

    @Test
    fun httpErrorContainsStatusCode() {
        val e = KokapiException.HttpError(404, "Not Found")
        assertEquals(404, e.code)
    }

    @Test
    fun notAuthenticatedIsKokapiException() {
        val e: KokapiException = KokapiException.NotAuthenticated
        assertIs<KokapiException>(e)
    }

    @Test
    fun exhaustiveWhen() {
        val exceptions: List<KokapiException> = listOf(
            KokapiException.ApiError("x", "y"),
            KokapiException.HttpError(500, "err"),
            KokapiException.NotAuthenticated,
            KokapiException.NetworkError,
            KokapiException.Unknown(null),
        )
        for (e in exceptions) {
            val label = when (e) {
                is KokapiException.ApiError -> "api"
                is KokapiException.HttpError -> "http"
                KokapiException.NotAuthenticated -> "auth"
                KokapiException.NetworkError -> "network"
                is KokapiException.Unknown -> "unknown"
            }
            assert(label.isNotEmpty())
        }
    }
}
