package pl.kokapi.endpoints

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.enum.LogType
import pl.kokapi.model.User

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

fun CachesClient.search(
    lat: Double,
    lon: Double,
    radiusKm: Double = 10.0,
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { search(lat, lon, radiusKm, filters) }
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
