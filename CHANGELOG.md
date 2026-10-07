# Changelog

All notable changes to KoKAPI are documented here. Follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format.

## [Unreleased]

## [0.2.0] — Quality pass

### Added

- `SearchFilters` data class — replaces 25 repeated parameters across `search`, `bbox`, `searchAll`, `searchSave`, `searchAndRetrieve`, and `searchFlow`.
- `UsersClient.byUuids()` — batch user lookup by UUID list.
- `UsersClient.byInternalId()` / `byInternalIds()` — lookup by site-internal numeric ID.
- `UsersClient.me()` — returns the currently authenticated user's profile.
- `FormattersClient.garmin()` / `ggz()` — Garmin and GGZ export formats.
- `FormattersClient.mapTile()` — map tile image endpoint.
- `AuthClientTest` — 13 unit tests covering the full OAuth flow.
- 147 tests total: 119 unit (mock HTTP) + 28 integration (live opencaching.pl).

### Changed

- **Breaking:** All `CachesClient` search methods now accept `SearchFilters` instead of individual filter parameters:
  - `search(lat, lon, radiusKm, filters)`
  - `bbox(southLat, westLon, northLat, eastLon, filters)`
  - `searchAll(filters)`
  - `searchSave(filters)`
  - `searchAndRetrieve(filters)`
  - `searchFlow(lat, lon, radiusKm, pageSize, filters)`
- `CachesClient.defaultFields` is now built from a grouped list of field names (identity, ratings, content…) rather than a single opaque pipe-string.

### Fixed

- `AuthClient.startLogin()` now throws `KokapiException.ApiError` (codes `oauth_token_missing` / `oauth_token_secret_missing`) instead of `IllegalStateException` — errors are now catchable as `KokapiException`.
- Integration tests now correctly skip via `org.junit.Assume.assumeTrue` when `OKAPI_CONSUMER_KEY` is not set (previously they failed instead of skipping).

---

## [0.1.0] — Initial release milestone

### Added

#### Core library (`kokapi-core`)

- Kotlin Multiplatform library targeting Android, JVM/Desktop, iOS (macOS builds), and wasmJs.
- `KokapiClient` entry point with pluggable `TokenStorage` and optional `HttpClient`.
- `KokapiSite` enum for all known Opencaching installations.
- 100% coverage of all 47 OKAPI endpoints across 9 clients.

##### Endpoints

| Client | Methods |
|---|---|
| `CachesClient` | `get`, `getMultiple`, `search`, `bbox`, `searchAll`, `searchByUrls`, `searchSave`, `searchAndRetrieve`, `searchFlow`, `mark`, `savePersonalNotes`, `edit` |
| `LogsClient` | `get`, `getEntries`, `getForCache`, `getUserLogs`, `submit`, `delete`, `edit`, `capabilities`, `addImage`, `editImage`, `deleteImage` |
| `UsersClient` | `byUsername`, `byUsernames`, `byUuid`, `byUuids`, `byInternalId`, `byInternalIds`, `me` |
| `AttrsClient` | `attributeIndex`, `attribute`, `attributes` |
| `ReplicateClient` | `info`, `currentRevision`, `changelog`, `changelogAll`, `fullDump` |
| `ApiSrvClient` | `installation`, `installations`, `stats` |
| `ApiRefClient` | `methodIndex`, `method`, `reportIssue` |
| `FormattersClient` | `gpx`, `garmin`, `ggz`, `mapTile` |
| `AuthClient` | `startLogin`, `completeLogin`, `logout`, `isAuthenticated` |

##### Models

- `Geocache` — 50+ fields including multilingual names/descriptions/hints, protection areas, latest logs, attributes, and auth-gated fields (`isFound`, `myNotes`, etc.)
- `GeocacheLog` — with extended fields (`location`, `ocTeamEntry`, `needsMaintenance`)
- `User` — with `rcmdFoundsNeeded`
- `ReplicateInfo`, `Changelog`, `ChangelogEntry` — full replication model with `ChangelogObjectType` enum
- `ProtectionArea` — typed object (not a raw string)
- `LogCapabilities`, `LogImage`, `CacheImage`, `Trackable`, `AltWaypoint`
- `Installation`, `ApiStats`, `ApiMethod`

##### Real-API correctness (opencaching.pl quirks)

- `needs_maintenance2` returns the string `"null"` — mapped correctly to `null`
- `latest_logs` entries omit `cache_code` — field is optional with empty default
- `protection_areas` returns objects `{type, name}` — not plain strings
- Auth-required fields excluded from default field list when unauthenticated
- `images=false` rejected by `formatters/gpx` — boolean params omitted when false
- `users/by_usernames` returns `null` for missing users — filtered out
- `caches/shortcuts/search_and_retrieve` requires `wrap=true`
- `replicate/changelog?since=0` rejected — `currentRevision()` uses `replicate/info`

#### Example app (`example-app`)

- Compose Multiplatform UI covering all endpoints across dedicated screens.
- `CachesScreen`, `LogsScreen`, `UsersScreen`, `AttrsScreen`, `ReplicateScreen`, `ApiInfoScreen`, `ApiRefScreen`, `FormattersScreen`.

#### Tests

- 119 unit tests with mock HTTP engine (commonTest).
- 28 live integration tests (`desktopTest`) against real opencaching.pl OKAPI, skipped when `OKAPI_CONSUMER_KEY` is not set.
