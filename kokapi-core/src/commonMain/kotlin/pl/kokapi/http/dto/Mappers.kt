package pl.kokapi.http.dto

import pl.kokapi.model.*
import pl.kokapi.model.Installation
import pl.kokapi.model.ApiStats
import pl.kokapi.model.ReplicateInfo
import pl.kokapi.model.LogCapabilities
import pl.kokapi.model.`enum`.CacheType
import pl.kokapi.model.`enum`.CacheStatus
import pl.kokapi.model.`enum`.LogType

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
    gcCode = gcCode,
    size2 = size2,
    recommendations = recommendations,
    reqPasswd = reqPasswd,
    shortDescription = shortDescription,
    images = images?.map { it.toDomain() } ?: emptyList(),
    trackablesCount = trackablesCount,
    trackables = trackables?.map { it.toDomain() } ?: emptyList(),
    altWpts = altWpts?.map { it.toDomain() } ?: emptyList(),
    country = country,
    region = region,
    rating = rating,
    tripTime = tripTime,
    tripDistance = tripDistance,
    dateHidden = dateHidden,
    lastModified = lastModified,
    isFound = isFound,
    isWatched = isWatched,
    isNotFound = isNotFound,
    isIgnored = isIgnored,
    watchers = watchers,
    ratingVotes = ratingVotes,
    myRating = myRating,
    willattends = willattends,
    needsMaintenance = needsMaintenance,
    myNotes = myNotes,
    latestLogs = latestLogs?.map { it.toDomain() } ?: emptyList(),
)

internal fun CacheImageDto.toDomain() = CacheImage(
    uuid = uuid,
    url = url,
    thumbUrl = thumbUrl,
    caption = caption,
    isSpoiler = isSpoiler,
)

internal fun TrackableDto.toDomain() = Trackable(
    code = code,
    name = name,
    url = url,
)

internal fun AltWaypointDto.toDomain() = AltWaypoint(
    name = name,
    location = location.toLatLon(),
    type = type,
    typeName = typeName,
    description = description,
)

internal fun UserRefDto.toDomain() = UserRef(uuid = uuid, username = username)

internal fun AttributeDto.toDomain() = Attribute(
    acode = acode,
    name = name,
    isYes = !acode.endsWith("-0"),
    iconUrl = iconUrl,
    description = description,
)

internal fun GeocacheLogDto.toDomain() = GeocacheLog(
    uuid = uuid,
    cacheCode = cacheCode,
    user = user.toDomain(),
    type = LogType.fromString(type),
    date = date,
    comment = comment,
    images = images?.map { it.toDomain() } ?: emptyList(),
    wasRecommended = wasRecommended,
    needsMaintenance = needsMaintenance,
    dateCreated = dateCreated,
    lastModified = lastModified,
    location = location,
    ocTeamEntry = ocTeamEntry,
)

internal fun LogImageDto.toDomain() = LogImage(
    uuid = uuid,
    url = url,
    thumbUrl = thumbUrl,
    caption = caption,
    isSpoiler = isSpoiler,
)

internal fun UserDto.toDomain() = User(
    uuid = uuid,
    username = username,
    profileUrl = profileUrl,
    cachesFound = cachesFound,
    hiddenCount = hiddenCount,
    rcmdsGiven = rcmdsGiven,
    rcmdsLeft = rcmdsLeft,
    rcmdFoundsNeeded = rcmdFoundsNeeded,
    homeLocation = homeLocation?.toLatLon(),
)

internal fun AttributeFullDto.toDomain() = Attribute(
    acode = acode,
    name = name,
    isYes = !acode.endsWith("-0"),
    iconUrl = localIconUrl,
    description = description,
)

internal fun LogCapabilitiesDto.toDomain() = LogCapabilities(
    canAdd = canAdd,
    availableTypes = availableTypes,
    requiresPassword = requiresPassword,
    canRecommend = canRecommend,
    canRate = canRate,
)

internal fun InstallationDto.toDomain() = Installation(
    siteUrl = siteUrl,
    okapiBaseUrl = okapiBaseUrl,
    siteName = siteName,
    okapiRevision = okapiRevision,
    registrationUrl = registrationUrl,
)

internal fun ApiStatsDto.toDomain() = ApiStats(
    cacheCount = cacheCount,
)

internal fun ReplicateInfoDto.toDomain() = ReplicateInfo(
    revision = revision,
    fulldumpUrl = fulldumpUrl,
)
