# KoKAPI — KMP Library for Opencaching OKAPI

**Date:** 2026-10-06

## Overview

KoKAPI is a Kotlin Multiplatform (KMP) library that wraps the Opencaching OKAPI REST API. It provides typed, idiomatic Kotlin access to all OKAPI endpoint groups across Android, iOS, and Desktop (JVM) platforms.

## Goals

- Cover all OKAPI endpoint groups: `caches`, `logs`, `users`, `attrs`, `replicate`, `oauth`
- Support all 4 OKAPI auth levels (0–3) with transparent OAuth 1.0a management
- Idiomatic Kotlin API: suspend functions + Flow for streaming + callback wrappers for iOS/Java
- Ship with tests, Dokka docs, and a working example app

## Architecture

### Module structure

```
kokapi/
├── kokapi-core/          # Common logic: models, client interfaces, OAuth, HTTP
├── kokapi-android/       # actual: TokenStorage (EncryptedSharedPrefs)
├── kokapi-ios/           # actual: TokenStorage (Keychain)
├── kokapi-desktop/       # actual: TokenStorage (file on disk)
├── kokapi-test/          # FakeKokapiClient for library consumers
├── docs/                 # MkDocs or Dokka-generated API docs
└── example-app/          # KMM example app (Android + iOS + Desktop)
    ├── androidApp/
    ├── iosApp/
    └── desktopApp/
```

### Entry point

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "...",
    consumerSecret = "...",
    callbackUrl = "kokapi://oauth"
)

// Level 0/1 — no login required
val cache = kokapi.caches.get("OP1234")

// Level 3 — after OAuth login
kokapi.auth.login()
val logs = kokapi.logs.submit(cacheCode = "OP1234", type = LogType.FOUND)
```

### Endpoint clients

Each OKAPI endpoint group maps to a typed client:

| Client | OKAPI group | Key methods |
|---|---|---|
| `CachesClient` | `caches` | `get`, `search`, `bbox`, `nearest` |
| `LogsClient` | `logs` | `get`, `submit`, `delete` |
| `UsersClient` | `users` | `byUsername`, `byUuid`, `me` |
| `AttrsClient` | `attrs` | `attributes`, `attribute` |
| `ReplicateClient` | `replicate` | `changelog`, `fullDump` |
| `OAuthClient` | `oauth` | `login`, `logout`, `isAuthenticated` |

### HTTP layer

- Ktor Client with platform-specific engines:
  - Android: `ktor-client-okhttp`
  - iOS: `ktor-client-darwin`
  - Desktop/JVM: `ktor-client-cio`
- Serialization: `kotlinx.serialization`
- OAuth 1.0a signing via Ktor auth plugin

## Authorization

Four levels:

| Level | Requirement | Access |
|---|---|---|
| 0 | None | Public data, no rate limiting |
| 1 | `consumerKey` | Public data with app identification |
| 2 | OAuth consumer signature | Signed requests |
| 3 | OAuth consumer + user token | Private user data |

`OAuthManager` handles the full OAuth 1.0a flow:
1. `requestToken()` — get temporary token
2. `getAuthorizationUrl()` — URL to open in browser
3. `handleCallback(url)` — parse verifier from redirect
4. `accessToken()` — exchange for persistent access token

Token storage is `expect/actual` per platform:
- Android: `EncryptedSharedPreferences`
- iOS: `Keychain`
- Desktop: encrypted file in user home dir

## Error handling

```kotlin
sealed class KokapiException : Exception() {
    data class HttpError(val code: Int, val reason: String) : KokapiException()
    data class ApiError(val errorCode: String, val message: String) : KokapiException()
    object NotAuthenticated : KokapiException()
    object NetworkError : KokapiException()
    data class Unknown(override val cause: Throwable?) : KokapiException()
}
```

- HTTP 4xx with OKAPI JSON body → `ApiError`
- HTTP 401 without token → `NotAuthenticated`
- No network → `NetworkError`
- Other → `Unknown`

## API style

```kotlin
// Suspend (primary)
val cache: Geocache = kokapi.caches.get("OP1234")

// Flow (for paginated/streaming results)
kokapi.caches.search(query).collect { cache -> ... }

// Callback wrappers (iOS/Java interop)
kokapi.caches.get("OP1234",
    onSuccess = { cache -> ... },
    onFailure = { e -> ... }
)
```

## Data models

```kotlin
data class Geocache(
    val code: String,
    val name: String,
    val location: LatLon,
    val type: CacheType,
    val status: CacheStatus,
    val difficulty: Float,
    val terrain: Float,
    val owner: UserRef,
    val attributes: List<Attribute>
)

data class LatLon(val lat: Double, val lon: Double)
enum class CacheType { TRADITIONAL, MULTI, QUIZ, VIRTUAL, OTHER }
enum class CacheStatus { AVAILABLE, TEMPORARILY_UNAVAILABLE, ARCHIVED }

data class GeocacheLog(
    val uuid: String,
    val cacheCode: String,
    val user: UserRef,
    val type: LogType,
    val date: String,
    val comment: String
)

enum class LogType { FOUND, NOT_FOUND, COMMENT, ATTENDED, WILL_ATTEND }

data class User(
    val uuid: String,
    val username: String,
    val profileUrl: String,
    val cacheCount: Int
)

data class Attribute(
    val acode: String,
    val name: String,
    val isYes: Boolean
)
```

## Gradle dependencies

```kotlin
commonMain {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}
androidMain { implementation("io.ktor:ktor-client-okhttp:$ktorVersion") }
iosMain { implementation("io.ktor:ktor-client-darwin:$ktorVersion") }
jvmMain { implementation("io.ktor:ktor-client-cio:$ktorVersion") }
```

## Testing

- `commonTest`: unit tests per endpoint client using Ktor `MockEngine`
- Tests cover: successful responses, each `KokapiException` type, OAuth flow
- `kokapi-test` module: `FakeKokapiClient` with configurable responses for library consumers

## Documentation

- Dokka for API reference (auto-generated)
- `docs/` markdown: Getting Started, OAuth Guide, endpoint examples
- README with quickstart

## Example app

Simple KMM app demonstrating:
1. OAuth login flow
2. Search geocaches by location
3. Display cache list with details
4. Submit a "Found it" log

Available as Android app, iOS app, and Desktop (JVM) app.

## Publication

Maven artifact: `pl.kokapi:kokapi-core`, published to Maven Central or GitHub Packages.
