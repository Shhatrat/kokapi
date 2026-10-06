package pl.kokapi.model

data class Geocache(
    val code: String,
    val name: String,
    val location: LatLon,
    val type: CacheType,
    val status: CacheStatus,
    val difficulty: Float,
    val terrain: Float,
    val owner: UserRef,
    val attributes: List<Attribute>,
    val description: String? = null,
    val hint: String? = null,
    val url: String? = null,
    val founds: Int = 0,
    val notFounds: Int = 0,
    val dateCreated: String? = null,
    val lastFound: String? = null,
)
