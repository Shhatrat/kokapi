# Changelog

All notable changes to KoKAPI are documented here. Follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format.

## [Unreleased]

### Changed
- **Breaking:** `CachesClient` search methods now take a `SearchFilters` object instead of individual filter parameters.
  - `search(lat, lon, radiusKm, filters)` replaces `search(lat, lon, radiusKm, limit, offset, type, ...)`
  - `bbox(s, w, n, e, filters)` replaces `bbox(s, w, n, e, limit, offset, type, ...)`
  - `searchAll(filters)` replaces `searchAll(type, status, terrain, ...)`
  - `searchSave(filters)` replaces `searchSave(type, status, terrain, ...)`
  - `searchAndRetrieve(filters)` replaces `searchAndRetrieve(type, status, terrain, ...)`
  - `searchFlow(lat, lon, radiusKm, pageSize, filters)` gains optional `filters` parameter
- `defaultFields` in `CachesClient` is now constructed from an organised list of field names rather than a single pipe-concatenated string, making the field list easier to read and maintain.

### Fixed
- Integration tests (`OkapiIntegrationTest`) now correctly skip (JUnit assumption) instead of failing when `OKAPI_CONSUMER_KEY` is not set.

---

## [0.1.0] — Initial release milestone

This covers all work from project inception through the first full-coverage pass.

### Added

#### Core library (`kokapi-core`)

- Kotlin Multiplatform library targeting Android, JVM/Desktop, iOS (macOS builds), and wasmJs.
- `KokapiClient` entry point with pluggable `TokenStorage` and optional `HttpClient`.
- `KokapiSite` enum for all known Opencaching installations.

##### Endpoints

| Client | Methods |
|---|---|
| `CachesClient` | `get`, `getMultiple`, `search`, `bbox`, `searchAll`, `searchByUrls`, `searchSave`, `searchAndRetrieve`, `searchFlow`, `mark`, `savePersonalNotes`, `edit` |
| `LogsClient` | `get`, `getEntries`, `getForCache`, `getUserLogs`, `submit`, `delete`, `edit`, `capabilities`, `addImage`, `editImage`, `deleteImage` |
| `UsersClient` | `byUsername`, `byUsernames`, `byUuid` |
| `AttrsClient` | `attributeIndex`, `attribute`, `attributes` |
| `ReplicateClient` | `info`, `currentRevision`, `changelog`, `changelogAll`, `fullDump` |
| `ApiSrvClient` | `installation`, `installations`, `stats` |
| `ApiRefClient` | `methodIndex`, `method` |
| `FormattersClient` | `gpx` |
| `AuthClient` | OAuth PKCE flow — `startLogin`, `handleCallback`, `logout` |

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

- 100+ unit tests with mock HTTP engine (commonTest).
- Live integration test suite (`desktopTest`) — 30 tests against real opencaching.pl OKAPI, skipped when `OKAPI_CONSUMER_KEY` is not set.
