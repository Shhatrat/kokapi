package pl.kokapi.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import pl.kokapi.KokapiSite
import pl.kokapi.auth.OAuthManager
import pl.kokapi.error.KokapiException

internal val json = Json { ignoreUnknownKeys = true; isLenient = true }

internal class HttpEngine(
    private val site: KokapiSite,
    private val oauthManager: OAuthManager,
    private val httpClient: HttpClient,
) {
    suspend fun get(path: String, params: Map<String, String> = emptyMap()): String {
        val allParams = params + ("consumer_key" to oauthManager.consumerKey)
        val url = buildUrl(path, allParams)
        return try {
            val response = httpClient.get(url) {
                if (oauthManager.isAuthenticated) {
                    header(HttpHeaders.Authorization, oauthManager.buildAuthHeader("GET", url, params))
                }
            }
            handleResponse(response)
        } catch (e: KokapiException) {
            throw e
        } catch (e: Exception) {
            throw KokapiException.NetworkError
        }
    }

    suspend fun post(path: String, params: Map<String, String> = emptyMap()): String {
        val url = "${site.baseUrl}/$path"
        val bodyString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return try {
            val response = httpClient.post(url) {
                if (oauthManager.isAuthenticated) {
                    header(HttpHeaders.Authorization, oauthManager.buildAuthHeader("POST", url, params))
                }
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(bodyString)
            }
            handleResponse(response)
        } catch (e: KokapiException) {
            throw e
        } catch (e: Exception) {
            throw KokapiException.NetworkError
        }
    }

    private fun buildUrl(path: String, params: Map<String, String>): String {
        val builder = URLBuilder("${site.baseUrl}/$path")
        params.forEach { (key, value) -> builder.parameters.append(key, value) }
        return builder.buildString()
    }

    private suspend fun handleResponse(response: HttpResponse): String {
        val body = response.bodyAsText()
        return when {
            response.status.isSuccess() -> body
            response.status == HttpStatusCode.Unauthorized ->
                throw KokapiException.NotAuthenticated
            response.status.value in 400..499 -> {
                val errorCode = extractErrorCode(body)
                throw KokapiException.ApiError(errorCode, body)
            }
            else -> throw KokapiException.HttpError(response.status.value, response.status.description)
        }
    }

    private fun extractErrorCode(body: String): String {
        // OKAPI format: {"error":{"reason_stack":["bad_request"],"developer_message":"..."}}
        val reasonStack = Regex(""""reason_stack"\s*:\s*\[\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
        if (reasonStack != null) return reasonStack
        // fallback: {"error":"string"}
        return Regex(""""error"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1) ?: "unknown_error"
    }
}
