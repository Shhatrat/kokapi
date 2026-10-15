<p align="center">
  <img src="logo.svg" alt="KoKAPI" width="270"/>
</p>

**Kotlin Multiplatform library for the [Opencaching OKAPI](https://opencaching.pl/okapi/introduction.html) REST API.**

Supports **Android**, **iOS**, and **Desktop (JVM)**. Full OAuth 1.0a support.

## Installation

Add GitHub Packages repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Shhatrat/kokapi")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("pl.kokapi:kokapi-core:1.0.0")
}
```

GitHub Packages requires authentication. Add your credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Generate a token at **GitHub → Settings → Developer settings → Personal access tokens** with the `read:packages` scope.

## Live Demo

Try the web demo at **https://shhatrat.github.io/kokapi/**

You will need an OKAPI consumer key — register one at **https://opencaching.pl/okapi/signup.html**

## Quickstart

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "YOUR_CONSUMER_KEY",
)

// Search caches near Krakow
val caches = kokapi.search.nearest(lat = 50.0619, lon = 19.9368, radiusKm = 5.0)
caches.forEach { println("${it.code}: ${it.name}") }

// Bounding-box search
val mapCaches = kokapi.search.bbox(
    southLat = 50.0, westLon = 19.8,
    northLat = 50.1, eastLon = 20.0,
)

// Get a single cache
val cache = kokapi.caches.get("OP1234")

// Stream all caches in an area (auto-paginated)
kokapi.search.flow(lat = 50.0619, lon = 19.9368).collect { c ->
    println(c.name)
}

// Export as GPX
val gpx = kokapi.formatters.gpx(listOf("OP1234", "OP5678"))
```

## OAuth Login

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "YOUR_CONSUMER_KEY",
    consumerSecret = "YOUR_CONSUMER_SECRET",
    callbackUrl = "yourapp://oauth",
    tokenStorage = FileTokenStorage(),   // or EncryptedPrefsTokenStorage(context) on Android
)

// Step 1: get URL to open in browser
val step = kokapi.auth.startLogin()
openBrowser(step.authorizeUrl)

// Step 2: after user authorises and is redirected back
val ok = kokapi.auth.completeLogin("yourapp://oauth?oauth_token=...&oauth_verifier=...")

// Step 3: use authenticated endpoints
val me = kokapi.users.me()
val logUuid = kokapi.logs.submit("OP1234", LogType.FOUND, "Awesome cache!")

// Attach a photo to the log
val imageUuid = kokapi.logs.addImage(logUuid, base64EncodedImage)

// Mark a cache as watched
kokapi.caches.mark("OP1234", watched = true)
```

## Supported Endpoints

### Caches

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.caches` | `get` | | Fetch a single geocache by code |
| `kokapi.caches` | `getMultiple` | | Fetch multiple geocaches by code list |
| `kokapi.caches` | `mark` | ✓ | Mark a cache as watched or ignored |
| `kokapi.caches` | `savePersonalNotes` | ✓ | Save private notes on a cache |
| `kokapi.caches` | `edit` | ✓ | Trigger cache-edit flow (owner/admin only) |

### Search

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.search` | `nearest` | | Nearest-caches search around a lat/lon point |
| `kokapi.search` | `bbox` | | Bounding-box search within a geographic rectangle |
| `kokapi.search` | `all` | | Full-featured search with 25+ filters (site-wide) |
| `kokapi.search` | `andRetrieve` | | Combined search + full data fetch in one request |
| `kokapi.search` | `save` | | Save a search on the server and return its set ID |
| `kokapi.search` | `byUrls` | | Find caches by their opencaching.pl URLs |
| `kokapi.search` | `flow` | | Auto-paginating Kotlin Flow for nearest search |
| `kokapi.search` | `bboxFlow` | | Auto-paginating Kotlin Flow for bbox search |
| `kokapi.search` | `allFlow` | | Auto-paginating Kotlin Flow for site-wide search |

### Logs

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.logs` | `get` | | Fetch a single log entry by UUID |
| `kokapi.logs` | `getEntries` | | Fetch multiple log entries by UUID list |
| `kokapi.logs` | `getForCache` | | Fetch recent logs for a specific cache |
| `kokapi.logs` | `getUserLogs` | ○ | Fetch logs submitted by a user |
| `kokapi.logs` | `submit` | ✓ | Submit a new log entry (Found, DNF, Comment, etc.) |
| `kokapi.logs` | `delete` | ✓ | Delete a log entry |
| `kokapi.logs` | `edit` | ✓ | Edit an existing log entry |
| `kokapi.logs` | `capabilities` | | Check available log types for a cache |
| `kokapi.logs` | `addImage` | ✓ | Attach a photo to a log entry |
| `kokapi.logs` | `editImage` | ✓ | Update a log photo's caption or spoiler flag |
| `kokapi.logs` | `deleteImage` | ✓ | Delete a log photo |

### Users

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.users` | `byUsername` | | Look up a user by display name |
| `kokapi.users` | `byUuid` | | Look up a user by OKAPI UUID |
| `kokapi.users` | `byUuids` | | Look up multiple users by UUID list |
| `kokapi.users` | `byUsernames` | | Look up multiple users by display name list |
| `kokapi.users` | `byInternalId` | | Look up a user by the site's internal numeric ID |
| `kokapi.users` | `byInternalIds` | | Look up multiple users by internal ID list |
| `kokapi.users` | `me` | ✓ | Fetch the authenticated user's own profile |

### Attributes

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.attrs` | `attribute` | | Fetch full details for a single attribute code |
| `kokapi.attrs` | `attributes` | | Fetch basic info for a list of attribute codes |
| `kokapi.attrs` | `attributeIndex` | | List all attributes supported by this installation |

### Formatters

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.formatters` | `gpx` | | Export caches as a GPX string |
| `kokapi.formatters` | `garmin` | | Export caches as a binary Garmin POI file |
| `kokapi.formatters` | `ggz` | | Export caches as a binary GGZ file |
| `kokapi.formatters` | `mapTile` | | Fetch a slippy-map PNG tile showing cache locations |

### API Server

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.apiSrv` | `installation` | | Metadata about the current OKAPI installation |
| `kokapi.apiSrv` | `installations` | | List all known OKAPI installations across all sites |
| `kokapi.apiSrv` | `stats` | | Usage statistics for this installation |

### API Reference

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.apiRef` | `method` | | Full description and field list for one OKAPI method |
| `kokapi.apiRef` | `methodIndex` | | Summary list of all methods on this installation |
| `kokapi.apiRef` | `reportIssue` | | Submit a bug report for an OKAPI method |

### Replicate

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.replicate` | `changelog` | | Incremental change events since a given revision |
| `kokapi.replicate` | `currentRevision` | | Current database revision number |
| `kokapi.replicate` | `info` | | Replication system metadata |
| `kokapi.replicate` | `fullDump` | | URL of the full database dump archive |

### Auth

| Client | Method | Auth required | Description |
|--------|--------|:---:|-------------|
| `kokapi.auth` | `startLogin` | | Step 1 of OAuth flow — get the authorisation URL |
| `kokapi.auth` | `completeLogin` | | Step 2 — exchange the verifier for an access token |
| `kokapi.auth` | `logout` | | Clear stored access token |
| `kokapi.auth` | `isAuthenticated` | | Check whether a valid access token is stored |

**Auth legend:** ✓ = required &nbsp;&nbsp; ○ = optional (more data with auth)

## Error Handling

```kotlin
try {
    val cache = kokapi.caches.get("OP1234")
} catch (e: KokapiException.ApiError) {
    println("API error: ${e.errorCode}")
} catch (e: KokapiException.NotAuthenticated) {
    println("Login required")
} catch (e: KokapiException.NetworkError) {
    println("No network")
}
```

## iOS / Java Callback API

```kotlin
// Callback wrappers for Swift/Java interop
kokapi.caches.get("OP1234", scope,
    onSuccess = { cache -> /* ... */ },
    onFailure = { e -> /* ... */ }
)
```

## Token Storage

| Platform | Class | Notes |
|---|---|---|
| Android | `EncryptedPrefsTokenStorage(context)` | AES-256 encrypted |
| iOS | `KeychainTokenStorage()` | System Keychain |
| Desktop/JVM | `FileTokenStorage()` | `~/.kokapi/tokens.properties` |
| All | `InMemoryTokenStorage()` | Testing only, not persistent |

## Supported OKAPI Sites

`OPENCACHING_PL`, `OPENCACHING_DE`, `OPENCACHING_US`, `OPENCACHING_NL`, `OPENCACHING_RO`, `OPENCACHE_UK`

## Building

```bash
# JVM / Desktop
./gradlew :kokapi-core:jvmJar

# Android
./gradlew :kokapi-core:assembleDebug

# iOS (macOS only)
./gradlew :kokapi-core:linkDebugFrameworkIosSimulatorArm64

# Run all tests
./gradlew :kokapi-core:desktopTest

# Desktop example
OKAPI_CONSUMER_KEY=your_key ./gradlew :example-app:desktopApp:desktopRun
```

## License

MIT
