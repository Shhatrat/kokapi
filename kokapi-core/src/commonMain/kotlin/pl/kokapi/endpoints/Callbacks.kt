package pl.kokapi.endpoints

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Attribute
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogCapabilities
import pl.kokapi.model.User
import pl.kokapi.model.enum.LogType

// ── caches ────────────────────────────────────────────────────────────────────

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

fun CachesClient.mark(
    cacheCode: String,
    watched: Boolean? = null,
    ignored: Boolean? = null,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { mark(cacheCode, watched, ignored) }
        .onSuccess { onSuccess() }
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CachesClient.savePersonalNotes(
    cacheCode: String,
    notes: String,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { savePersonalNotes(cacheCode, notes) }
        .onSuccess { onSuccess() }
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CachesClient.edit(
    cacheCode: String,
    passwd: String? = null,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { edit(cacheCode, passwd) }
        .onSuccess { onSuccess() }
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

fun CacheSearchClient.byUrls(
    urls: List<String>,
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byUrls(urls) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun CacheSearchClient.save(
    filters: SearchFilters = SearchFilters(),
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { save(filters) }
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

fun LogsClient.getEntries(
    logUuids: List<String>,
    scope: CoroutineScope,
    onSuccess: (Map<String, GeocacheLog>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { getEntries(logUuids) }
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

fun LogsClient.getUserLogs(
    userUuid: String,
    limit: Int = 20,
    scope: CoroutineScope,
    onSuccess: (List<GeocacheLog>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { getUserLogs(userUuid, limit) }
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

fun LogsClient.addImage(
    logUuid: String,
    imageBase64: String,
    caption: String? = null,
    isSpoiler: Boolean = false,
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { addImage(logUuid, imageBase64, caption, isSpoiler) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.editImage(
    imageUuid: String,
    caption: String? = null,
    isSpoiler: Boolean? = null,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { editImage(imageUuid, caption, isSpoiler) }
        .onSuccess { onSuccess() }
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun LogsClient.deleteImage(
    imageUuid: String,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { deleteImage(imageUuid) }
        .onSuccess { onSuccess() }
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

fun UsersClient.byUuids(
    uuids: List<String>,
    scope: CoroutineScope,
    onSuccess: (Map<String, User>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byUuids(uuids) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun UsersClient.byUsernames(
    usernames: List<String>,
    scope: CoroutineScope,
    onSuccess: (Map<String, User>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byUsernames(usernames) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun UsersClient.byInternalId(
    internalId: String,
    scope: CoroutineScope,
    onSuccess: (User) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byInternalId(internalId) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun UsersClient.byInternalIds(
    internalIds: List<String>,
    scope: CoroutineScope,
    onSuccess: (Map<String, User>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { byInternalIds(internalIds) }
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

// ── attrs ─────────────────────────────────────────────────────────────────────

fun AttrsClient.attribute(
    acode: String,
    scope: CoroutineScope,
    onSuccess: (Attribute) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { attribute(acode) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun AttrsClient.attributes(
    acodes: List<String>,
    scope: CoroutineScope,
    onSuccess: (List<Attribute>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { attributes(acodes) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun AttrsClient.attributeIndex(
    scope: CoroutineScope,
    onSuccess: (List<Attribute>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { attributeIndex() }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

// ── formatters ────────────────────────────────────────────────────────────────

fun FormattersClient.gpx(
    cacheCodes: List<String>,
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { gpx(cacheCodes) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun FormattersClient.garmin(
    cacheCodes: List<String>,
    scope: CoroutineScope,
    onSuccess: (ByteArray) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { garmin(cacheCodes) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun FormattersClient.ggz(
    cacheCodes: List<String>,
    scope: CoroutineScope,
    onSuccess: (ByteArray) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { ggz(cacheCodes) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}

fun FormattersClient.mapTile(
    z: Int,
    x: Int,
    y: Int,
    scope: CoroutineScope,
    onSuccess: (ByteArray) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { mapTile(z, x, y) }
        .onSuccess(onSuccess)
        .onFailure { onFailure(it as? KokapiException ?: KokapiException.Unknown(it)) }
}
