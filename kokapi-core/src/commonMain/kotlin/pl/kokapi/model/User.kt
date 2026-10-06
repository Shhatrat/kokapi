package pl.kokapi.model

data class UserRef(val uuid: String, val username: String)

data class User(
    val uuid: String,
    val username: String,
    val profileUrl: String,
    val cachesFound: Int,
    val hiddenCount: Int,
    val rcmdsGiven: Int = 0,
    val rcmdsLeft: Int? = null,
    val homeLocation: LatLon? = null,
)
