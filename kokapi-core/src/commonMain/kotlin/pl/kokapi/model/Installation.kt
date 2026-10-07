package pl.kokapi.model

/** Metadata about an Opencaching site installation (returned by `apisrv/installation`). */
data class Installation(
    val siteUrl: String,
    val okapiBaseUrl: String,
    val siteName: String,
    val okapiRevision: Int,
    val registrationUrl: String?,
)

/** Site-wide statistics returned by `apisrv/stats`. */
data class ApiStats(
    val cacheCount: Int,
)
