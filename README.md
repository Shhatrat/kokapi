# KoKAPI

**Kotlin Multiplatform library for the [Opencaching OKAPI](https://opencaching.pl/okapi/introduction.html) REST API.**

Supports **Android**, **iOS**, and **Desktop (JVM)**. Full OAuth 1.0a support.

## Quickstart

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "YOUR_CONSUMER_KEY",
)

// Search caches near Krakow
val caches = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 5.0)
caches.forEach { println("${it.code}: ${it.name}") }

// Get a single cache
val cache = kokapi.caches.get("OP1234")

// Stream all caches in an area
kokapi.caches.searchFlow(lat = 50.0619, lon = 19.9368).collect { cache ->
    println(cache.name)
}
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

// Step 2: after user authorizes and is redirected back
val ok = kokapi.auth.completeLogin("yourapp://oauth?oauth_token=...&oauth_verifier=...")

// Step 3: use authenticated endpoints
val me = kokapi.users.me()
val logUuid = kokapi.logs.submit("OP1234", LogType.FOUND, "Awesome cache!")
```

## Supported Endpoints

| Client | Methods |
|---|---|
| `kokapi.caches` | `get`, `getMultiple`, `search`, `bbox`, `searchFlow` |
| `kokapi.logs` | `get`, `submit`, `delete` |
| `kokapi.users` | `byUsername`, `byUuid`, `me` |
| `kokapi.attrs` | `attributes`, `attribute` |
| `kokapi.replicate` | `changelog`, `currentRevision` |
| `kokapi.auth` | `startLogin`, `completeLogin`, `logout`, `isAuthenticated` |

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
