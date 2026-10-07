package pl.kokapi.endpoints

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogCapabilities
import pl.kokapi.model.User
import pl.kokapi.model.enum.LogType

// ── caches ───────────────────────────────────────────────────────────────────

fun CachesClient.get(
    code: String,
    scope: CoroutineScope,
    onSuccess: (Geocache) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { get(code) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CachesClient.getMultiple(
    codes: List<String>,
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { getMultiple(codes) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

// ── search ────────────────────────────────────────────────────────────────────

fun CacheSearchClient.nearest(
    lat: Double,
    lon: Double,
    radiusKm: Double = 10.0,
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { nearest(lat, lon, radiusKm, filters) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CacheSearchClient.bbox(
    southLat: Double,
    westLon: Double,
    northLat: Double,
    eastLon: Double,
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { bbox(southLat, westLon, northLat, eastLon, filters) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CacheSearchClient.all(
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { all(filters) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CacheSearchClient.andRetrieve(
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { andRetrieve(filters) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

// ── logs ──────────────────────────────────────────────────────────────────────

fun LogsClient.get(
    logUuid: String,
    scope: CoroutineScope,
    onSuccess: (GeocacheLog) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { get(logUuid) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.getForCache(
    cacheCode: String,
    limit: Int = 20,
    scope: CoroutineScope,
    onSuccess: (List<GeocacheLog>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { getForCache(cacheCode, limit) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.submit(
    cacheCode: String,
    type: LogType,
    comment: String = "",
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { submit(cacheCode, type, comment) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.delete(
    logUuid: String,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { delete(logUuid) }
        .onSuccess { onSuccess() }
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.edit(
    logUuid: String,
    comment: String? = null,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { edit(logUuid, comment = comment) }
        .onSuccess { onSuccess() }
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.capabilities(
    cacheCode: String,
    scope: CoroutineScope,
    onSuccess: (LogCapabilities) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { capabilities(cacheCode) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

// ── users ─────────────────────────────────────────────────────────────────────

fun UsersClient.byUsername(
    username: String,
    scope: CoroutineScope,
    onSuccess: (User) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byUsername(username) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun UsersClient.byUuid(
    uuid: String,
    scope: CoroutineScope,
    onSuccess: (User) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byUuid(uuid) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun UsersClient.me(
    scope: CoroutineScope,
    onSuccess: (User) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { me() }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}
