package pl.kokapi.model

import pl.kokapi.model.`enum`.CacheType
import pl.kokapi.model.`enum`.CacheStatus

/**
 * A single geocache as returned by the OKAPI server.
 *
 * Most fields are populated by default; optional fields are null when not requested
 * or not set by the cache owner.
 *
 * @property code OKAPI cache code (e.g. "OP1234").
 * @property location Coordinates of the cache.
 * @property difficulty Difficulty rating from 1.0 to 5.0 in 0.5 steps.
 * @property terrain Terrain rating from 1.0 to 5.0 in 0.5 steps.
 * @property owner Reference to the cache owner's profile.
 * @property hint Decoded hint text, or null if no hint is set.
 * @property founds Total number of Found-It logs.
 * @property notFounds Total number of Didn't-Find-It logs.
 * @property gcCode Corresponding Geocaching.com GC code, if the cache is listed there.
 * @property size2 Container size label (e.g. "regular", "small", "micro").
 * @property recommendations Number of user recommendations this cache has received.
 * @property reqPasswd Whether a password is required to log a find.
 * @property isFound Whether the authenticated user has found this cache; null when unauthenticated.
 * @property isWatched Whether the authenticated user is watching this cache; null when unauthenticated.
 */
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
    val gcCode: String? = null,
    val size2: String? = null,
    val recommendations: Int = 0,
    val reqPasswd: Boolean = false,
    val shortDescription: String? = null,
    val images: List<CacheImage> = emptyList(),
    val trackablesCount: Int = 0,
    val trackables: List<Trackable> = emptyList(),
    val altWpts: List<AltWaypoint> = emptyList(),
    val country: String? = null,
    val region: String? = null,
    val rating: Float? = null,
    val tripTime: Float? = null,
    val tripDistance: Float? = null,
    val dateHidden: String? = null,
    val lastModified: String? = null,
    val isFound: Boolean? = null,
    val isWatched: Boolean? = null,
    val isNotFound: Boolean? = null,
    val isIgnored: Boolean? = null,
    val watchers: Int = 0,
    val ratingVotes: Int? = null,
    val myRating: Int? = null,
    val willattends: Int = 0,
    val needsMaintenance: Boolean? = null,
    val myNotes: String? = null,
    val latestLogs: List<GeocacheLog> = emptyList(),
    val names: Map<String, String>? = null,
    val descriptions: Map<String, String>? = null,
    val shortDescriptions: Map<String, String>? = null,
    val hints2: Map<String, String>? = null,
    val isRecommended: Boolean? = null,
    val attrAcodes: List<String> = emptyList(),
    val attrNames: List<String> = emptyList(),
    val ocTeamAnnotation: String? = null,
    val attributionNote: String? = null,
    val distance: Double? = null,
    val bearing: String? = null,
    val internalId: Int? = null,
    val oxsize: Float? = null,
    val protectionAreas: List<ProtectionArea>? = null,
)
