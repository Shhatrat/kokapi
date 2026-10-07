package pl.kokapi.endpoints

/** Common filter parameters shared by all geocache search operations. */
data class SearchFilters(
    val type: List<String>? = null,
    val status: List<String> = listOf("Available"),
    val terrain: String? = null,
    val difficulty: String? = null,
    val size2: List<String>? = null,
    val ownerUuid: List<String>? = null,
    val name: String? = null,
    val minRcmds: String? = null,
    val minFounds: Int? = null,
    val maxFounds: Int? = null,
    val modifiedSince: String? = null,
    val foundStatus: String? = null,
    val foundBy: String? = null,
    val notFoundBy: String? = null,
    val recommendedOnly: Boolean = false,
    val recommendedBy: String? = null,
    val watchedOnly: Boolean = false,
    val ignoredStatus: String? = null,
    val excludeMyOwn: Boolean = false,
    val withTrackablesOnly: Boolean = false,
    val ftfHunter: Boolean = false,
    val setAnd: String? = null,
    val rating: String? = null,
    val orderBy: String? = null,
    val limit: Int = 100,
    val offset: Int = 0,
)

internal fun SearchFilters.toParams(): Map<String, String> = buildMap {
    put("status", status.joinToString("|"))
    put("limit", limit.toString())
    put("offset", offset.toString())
    type?.let { put("type", it.joinToString("|")) }
    terrain?.let { put("terrain", it) }
    difficulty?.let { put("difficulty", it) }
    size2?.let { put("size2", it.joinToString("|")) }
    ownerUuid?.let { put("owner_uuid", it.joinToString("|")) }
    name?.let { put("name", it) }
    minRcmds?.let { put("min_rcmds", it) }
    minFounds?.let { put("min_founds", it.toString()) }
    maxFounds?.let { put("max_founds", it.toString()) }
    modifiedSince?.let { put("modified_since", it) }
    foundStatus?.let { put("found_status", it) }
    foundBy?.let { put("found_by", it) }
    notFoundBy?.let { put("not_found_by", it) }
    if (recommendedOnly) put("recommended_only", "true")
    recommendedBy?.let { put("recommended_by", it) }
    if (watchedOnly) put("watched_only", "true")
    ignoredStatus?.let { put("ignored_status", it) }
    if (excludeMyOwn) put("exclude_my_own", "true")
    if (withTrackablesOnly) put("with_trackables_only", "true")
    if (ftfHunter) put("ftf_hunter", "true")
    setAnd?.let { put("set_and", it) }
    rating?.let { put("rating", it) }
    orderBy?.let { put("order_by", it) }
}
