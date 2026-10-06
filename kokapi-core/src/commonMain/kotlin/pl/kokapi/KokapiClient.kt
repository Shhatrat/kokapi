package pl.kokapi

import io.ktor.client.*
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.auth.OAuthManager
import pl.kokapi.auth.TokenStorage
import pl.kokapi.endpoints.*
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.createPlatformHttpClient

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

    val caches: CachesClient = CachesClient(engine)
    val logs: LogsClient = LogsClient(engine)
    val users: UsersClient = UsersClient(engine)
    val attrs: AttrsClient = AttrsClient(engine)
    val replicate: ReplicateClient = ReplicateClient(engine)
    val auth: AuthClient = AuthClient(oauthManager, engine)
    val apiSrv: ApiSrvClient = ApiSrvClient(engine)
    val formatters: FormattersClient = FormattersClient(engine)
    val apiRef: ApiRefClient = ApiRefClient(engine)
}
