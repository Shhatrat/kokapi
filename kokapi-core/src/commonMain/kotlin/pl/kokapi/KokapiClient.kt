package pl.kokapi

import io.ktor.client.*
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.auth.OAuthManager
import pl.kokapi.auth.TokenStorage
import pl.kokapi.endpoints.*
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.createPlatformHttpClient

/**
 * Main entry point for the KoKAPI library.
 *
 * Create one instance per OKAPI site and reuse it throughout your app.
 *
 * @param site The Opencaching site to connect to.
 * @param consumerKey OKAPI consumer key (obtain from the site's registration page).
 * @param consumerSecret OKAPI consumer secret; required for OAuth-authenticated endpoints.
 * @param callbackUrl OAuth callback URL registered for your app, or "oob" for out-of-band PIN flow.
 * @param tokenStorage Where to persist OAuth tokens between sessions.
 * @param httpClient Ktor [HttpClient] to use; defaults to a platform-appropriate client.
 */
class KokapiClient(
    val site: KokapiSite,
    val consumerKey: String,
    consumerSecret: String = "",
    callbackUrl: String = "oob",
    tokenStorage: TokenStorage = InMemoryTokenStorage(),
    httpClient: HttpClient = createPlatformHttpClient(),
) {
    private val oauthManager = OAuthManager(
        site = site,
        consumerKey = consumerKey,
        consumerSecret = consumerSecret,
        callbackUrl = callbackUrl,
        tokenStorage = tokenStorage,
    )

    private val engine = HttpEngine(site, oauthManager, httpClient)

    /** Geocache retrieval and management (get, mark, edit, personal notes). */
    val caches: CachesClient = CachesClient(engine)

    /** Geocache discovery — search by location, bbox, filters, URL, and more. */
    val search: CacheSearchClient = CacheSearchClient(engine, caches)

    /** Log entry access and submission. */
    val logs: LogsClient = LogsClient(engine)

    /** User profile lookup. */
    val users: UsersClient = UsersClient(engine)

    /** Cache attribute definitions. */
    val attrs: AttrsClient = AttrsClient(engine)

    /** Database replication and full-dump access. */
    val replicate: ReplicateClient = ReplicateClient(engine)

    /** OAuth 1.0a authentication flow. */
    val auth: AuthClient = AuthClient(oauthManager, engine)

    /** OKAPI server installation metadata and stats. */
    val apiSrv: ApiSrvClient = ApiSrvClient(engine)

    /** GPX/Garmin/GGZ export and map tile fetching. */
    val formatters: FormattersClient = FormattersClient(engine)

    /** OKAPI self-describing API reference. */
    val apiRef: ApiRefClient = ApiRefClient(engine)
}
