package pl.kokapi.http.dto

import pl.kokapi.model.*

internal fun GeocacheDto.toDomain() = Geocache(
    code = code,
    name = name,
    location = location.toLatLon(),
    type = CacheType.fromString(type),
    status = CacheStatus.fromString(status),
    difficulty = difficulty,
    terrain = terrain,
    owner = owner?.toDomain() ?: UserRef("", ""),
    attributes = attributes?.map { it.toDomain() } ?: emptyList(),
    description = description,
    hint = hint,
    url = url,
    founds = founds,
    notFounds = notFounds,
    dateCreated = dateCreated,
    lastFound = lastFound,
)

internal fun UserRefDto.toDomain() = UserRef(uuid = uuid, username = username)

internal fun AttributeDto.toDomain() = Attribute(
    acode = acode,
    name = name,
    isYes = !acode.endsWith("-0"),
)

internal fun GeocacheLogDto.toDomain() = GeocacheLog(
    uuid = uuid,
    cacheCode = cacheCode,
    user = user.toDomain(),
    type = LogType.fromString(type),
    date = date,
    comment = comment,
)

internal fun UserDto.toDomain() = User(
    uuid = uuid,
    username = username,
    profileUrl = profileUrl,
    cachesFound = cachesFound,
    hiddenCount = hiddenCount,
)

internal fun AttributeFullDto.toDomain() = Attribute(
    acode = acode,
    name = name,
    isYes = !acode.endsWith("-0"),
)
