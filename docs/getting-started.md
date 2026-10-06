# Getting Started with KoKAPI

## 1. Get a Consumer Key

Register your application at the [Opencaching OKAPI](https://opencaching.pl/okapi/introduction.html)
to receive a `consumer_key` and `consumer_secret`.

## 2. Create a KokapiClient

```kotlin
// Desktop / JVM
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "your_consumer_key",
    consumerSecret = "your_consumer_secret",
    callbackUrl = "yourapp://oauth",
    tokenStorage = FileTokenStorage(),
)

// Android
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "your_consumer_key",
    consumerSecret = "your_consumer_secret",
    callbackUrl = "yourapp://oauth",
    tokenStorage = EncryptedPrefsTokenStorage(context),
)

// iOS (Swift)
let kokapi = KokapiClient(
    site: KokapiSite.opencachingPl,
    consumerKey: "your_consumer_key",
    consumerSecret: "your_consumer_secret",
    callbackUrl: "yourapp://oauth",
    tokenStorage: KeychainTokenStorage()
)
```

## 3. Search for Geocaches

```kotlin
// By location + radius
val caches = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 10.0)

// By bounding box
val caches = kokapi.caches.bbox(
    southLat = 49.9, westLon = 19.8,
    northLat = 50.2, eastLon = 20.1
)

// Paginated flow (all results)
kokapi.caches.searchFlow(lat = 50.0619, lon = 19.9368).collect { cache ->
    println("${cache.code}: ${cache.name}")
}
```

## 4. Fetch a Single Cache

```kotlin
val cache = kokapi.caches.get("OP1234")
println("Name: ${cache.name}")
println("Difficulty: ${cache.difficulty} / Terrain: ${cache.terrain}")
println("Location: ${cache.location.lat}, ${cache.location.lon}")
```

## 5. OAuth Login

```kotlin
// Start the OAuth flow
val step = kokapi.auth.startLogin()
// Open step.authorizeUrl in a browser / WebView

// After the user authorizes and is redirected back:
val callbackUrl = "yourapp://oauth?oauth_token=xxx&oauth_verifier=yyy"
val success = kokapi.auth.completeLogin(callbackUrl)
```

## 6. Submit a Log

```kotlin
// Requires OAuth (Level 3)
val logUuid = kokapi.logs.submit(
    cacheCode = "OP1234",
    type = LogType.FOUND,
    comment = "Great cache, TFTC!"
)
```

## 7. Handle Errors

```kotlin
try {
    val cache = kokapi.caches.get("INVALID")
} catch (e: KokapiException.ApiError) {
    // OKAPI returned an error response
    println("Error: ${e.errorCode} — ${e.message}")
} catch (e: KokapiException.NotAuthenticated) {
    // Method requires OAuth but user is not logged in
    println("Please log in first")
} catch (e: KokapiException.NetworkError) {
    // No network connection
    println("Check your internet connection")
} catch (e: KokapiException.HttpError) {
    // Unexpected HTTP status
    println("HTTP ${e.code}: ${e.reason}")
}
```
