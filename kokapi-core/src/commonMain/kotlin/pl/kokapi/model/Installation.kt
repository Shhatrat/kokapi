package pl.kokapi.model

data class Installation(
    val siteUrl: String,
    val okapiBaseUrl: String,
    val siteName: String,
    val okapiRevision: Int,
    val registrationUrl: String?,
)

data class ApiStats(
    val cacheCount: Int,
)
