package pl.kokapi.model

/** Minimal reference to a user — UUID and display name. Used wherever a full [User] is not needed. */
data class UserRef(val uuid: String, val username: String)

/**
 * Full geocacher profile as returned by the users endpoints.
 *
 * @property uuid Unique OKAPI user identifier.
 * @property hiddenCount Number of caches the user has hidden.
 * @property rcmdsGiven Number of recommendations the user has given to other caches.
 * @property rcmdsLeft Remaining recommendations the user can still give this period; null when not available.
 * @property homeLocation The user's registered home coordinates; null if not set.
 */
data class User(
    val uuid: String,
    val username: String,
    val profileUrl: String,
    val cachesFound: Int,
    val hiddenCount: Int,
    val rcmdsGiven: Int = 0,
    val rcmdsLeft: Int? = null,
    val rcmdFoundsNeeded: Int = 0,
    val homeLocation: LatLon? = null,
)
