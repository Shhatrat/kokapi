# KoKAPI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Kotlin Multiplatform library (Android + iOS + Desktop/JVM) wrapping the Opencaching OKAPI REST API with full OAuth 1.0a support, typed endpoint clients, and a working example app.

**Architecture:** Single `kokapi-core` module with commonMain logic and platform source sets (androidMain, iosMain, jvmMain) for crypto and HTTP engine. `OAuthManager` handles the full OAuth 1.0a flow transparently. Each OKAPI endpoint group maps to a typed client exposed via `KokapiClient`.

**Tech Stack:** Kotlin 2.0.21, KMP, Ktor 3.0.1, kotlinx.serialization 1.7.3, kotlinx.coroutines 1.9.0, AGP 8.5.2, Gradle 8.9, Dokka 1.9.20.

## Global Constraints

- Package: `pl.kokapi` everywhere
- iOS targets only compile on macOS; on Linux build with `./gradlew :kokapi-core:jvmJar` or Android targets
- All suspend functions throw `KokapiException` subtypes — never raw HTTP or IO exceptions
- All JSON mapping goes through internal DTOs in `http/dto/`; domain models in `model/` are annotation-free
- `minSdk = 24`, `compileSdk = 35`, `targetSdk = 35`
- No `println` in library code; no logging dependency

---

### Task 1: Project Scaffolding

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `kokapi-core/build.gradle.kts`
- Create: `.gitignore`

**Interfaces:**
- Produces: compilable empty KMP project with androidTarget, jvm("desktop"), and conditional iOS targets

- [ ] **Step 1: Create `.gitignore`**

```
.gradle/
build/
*.iml
.idea/
local.properties
*.DS_Store
```

- [ ] **Step 2: Create `gradle/libs.versions.toml`**

```toml
[versions]
kotlin = "2.0.21"
agp = "8.5.2"
ktor = "3.0.1"
coroutines = "1.9.0"
serialization = "1.7.3"
dokka = "1.9.20"
security-crypto = "1.1.0-alpha06"
compose = "1.6.11"
compose-plugin = "1.6.11"

[libraries]
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { group = "io.ktor", name = "ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { group = "io.ktor", name = "ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-mock = { group = "io.ktor", name = "ktor-client-mock", version.ref = "ktor" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
android-application = { id = "com.android.application", version.ref = "agp" }
dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-plugin" }
```

- [ ] **Step 3: Create `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 4: Download Gradle wrapper**

```bash
cd /home/szymon/programming/kokapi
wget -q https://services.gradle.org/distributions/gradle-8.9-bin.zip -O /tmp/gradle-8.9-bin.zip 2>/dev/null || true
# If wget fails, create the wrapper jar via:
gradle wrapper --gradle-version 8.9 --distribution-type bin
```

- [ ] **Step 5: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "kokapi"

include(":kokapi-core")
include(":kokapi-test")
include(":example-app:androidApp")
include(":example-app:desktopApp")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

- [ ] **Step 6: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.dokka) apply false
}
```

- [ ] **Step 7: Create `kokapi-core/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
}

val hostOs = System.getProperty("os.name")
val isMacOs = hostOs.startsWith("Mac")

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "11" } }
    }
    if (isMacOs) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.security.crypto)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        if (isMacOs) {
            val iosMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
            listOf(
                getByName("iosX64Main"),
                getByName("iosArm64Main"),
                getByName("iosSimulatorArm64Main")
            ).forEach { it.dependsOn(iosMain) }
        }
    }
}

android {
    namespace = "pl.kokapi"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}
```

- [ ] **Step 8: Create `kokapi-core/src/commonMain/kotlin/pl/kokapi/.gitkeep`**

```bash
mkdir -p kokapi-core/src/commonMain/kotlin/pl/kokapi
mkdir -p kokapi-core/src/androidMain/kotlin/pl/kokapi/auth
mkdir -p kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth
mkdir -p kokapi-core/src/commonTest/kotlin/pl/kokapi
touch kokapi-core/src/commonMain/kotlin/pl/kokapi/.gitkeep
```

- [ ] **Step 9: Verify project syncs**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:tasks --quiet
```
Expected: task list printed without errors.

- [ ] **Step 10: Init git and commit**

```bash
cd /home/szymon/programming/kokapi
git init
git add .
git commit -m "chore: initial KMP project scaffolding"
```

---

### Task 2: Data Models + DTOs

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/KokapiSite.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Enums.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/LatLon.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Geocache.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/GeocacheLog.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/User.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Attribute.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/GeocacheDto.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/LogDto.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/UserDto.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/AttributeDto.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt`

**Interfaces:**
- Produces: `Geocache`, `GeocacheLog`, `User`, `UserRef`, `Attribute`, `LatLon`, all enums, DTOs, `toDomain()` mappers

- [ ] **Step 1: Write failing mapper tests**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt
package pl.kokapi.model

import pl.kokapi.http.dto.GeocacheDto
import pl.kokapi.http.dto.UserRefDto
import pl.kokapi.http.dto.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun geocacheDtoMapsToGeocache() {
        val dto = GeocacheDto(
            code = "OP1234",
            name = "Test Cache",
            location = "50.1234|20.5678",
            type = "Traditional",
            status = "Available",
            difficulty = 2.0f,
            terrain = 1.5f,
            owner = UserRefDto(uuid = "u1", username = "tester"),
            attributes = emptyList()
        )
        val cache = dto.toDomain()
        assertEquals("OP1234", cache.code)
        assertEquals("Test Cache", cache.name)
        assertEquals(50.1234, cache.location.lat, 0.0001)
        assertEquals(20.5678, cache.location.lon, 0.0001)
        assertEquals(CacheType.TRADITIONAL, cache.type)
        assertEquals(CacheStatus.AVAILABLE, cache.status)
        assertEquals(2.0f, cache.difficulty)
        assertEquals(1.5f, cache.terrain)
        assertEquals("tester", cache.owner.username)
    }

    @Test
    fun unknownCacheTypeFallsBackToOther() {
        val dto = GeocacheDto(
            code = "OP9", name = "X", location = "0.0|0.0",
            type = "SomeFutureType", status = "Available",
            difficulty = 1f, terrain = 1f, owner = UserRefDto("", ""), attributes = emptyList()
        )
        assertEquals(CacheType.OTHER, dto.toDomain().type)
    }

    @Test
    fun latLonParsedFromPipeString() {
        val ll = "51.5074|-0.1278".toLatLon()
        assertEquals(51.5074, ll.lat, 0.0001)
        assertEquals(-0.1278, ll.lon, 0.0001)
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd /home/szymon/programming/kokapi
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.model.MappersTest" 2>&1 | tail -20
```
Expected: compilation error — types not defined yet.

- [ ] **Step 3: Create `KokapiSite.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/KokapiSite.kt
package pl.kokapi

enum class KokapiSite(val baseUrl: String) {
    OPENCACHING_PL("https://opencaching.pl/okapi/services"),
    OPENCACHING_DE("https://www.opencaching.de/okapi/services"),
    OPENCACHING_US("https://www.opencaching.us/okapi/services"),
    OPENCACHING_NL("https://www.opencaching.nl/okapi/services"),
    OPENCACHING_RO("https://www.opencaching.ro/okapi/services"),
    OPENCACHE_UK("https://opencache.uk/okapi/services"),
}
```

- [ ] **Step 4: Create `model/Enums.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Enums.kt
package pl.kokapi.model

enum class CacheType {
    TRADITIONAL, MULTI, QUIZ, VIRTUAL, EVENT, WEBCAM, MOVING, PODCAST, OWN, OTHER;

    companion object {
        fun fromString(s: String): CacheType = when (s.lowercase()) {
            "traditional" -> TRADITIONAL
            "multi" -> MULTI
            "quiz" -> QUIZ
            "virtual" -> VIRTUAL
            "event" -> EVENT
            "webcam" -> WEBCAM
            "moving" -> MOVING
            "podcast" -> PODCAST
            "own" -> OWN
            else -> OTHER
        }
    }
}

enum class CacheStatus {
    AVAILABLE, TEMPORARILY_UNAVAILABLE, ARCHIVED, OTHER;

    companion object {
        fun fromString(s: String): CacheStatus = when (s.lowercase()) {
            "available" -> AVAILABLE
            "temporarily unavailable" -> TEMPORARILY_UNAVAILABLE
            "archived" -> ARCHIVED
            else -> OTHER
        }
    }
}

enum class LogType {
    FOUND, NOT_FOUND, COMMENT, ATTENDED, WILL_ATTEND, OTHER;

    val okapiName: String get() = when (this) {
        FOUND -> "Found it"
        NOT_FOUND -> "Didn't find it"
        COMMENT -> "Comment"
        ATTENDED -> "Attended"
        WILL_ATTEND -> "Will attend"
        OTHER -> "Comment"
    }

    companion object {
        fun fromString(s: String): LogType = when (s.lowercase()) {
            "found it" -> FOUND
            "didn't find it" -> NOT_FOUND
            "comment" -> COMMENT
            "attended" -> ATTENDED
            "will attend" -> WILL_ATTEND
            else -> OTHER
        }
    }
}
```

- [ ] **Step 5: Create `model/LatLon.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/LatLon.kt
package pl.kokapi.model

data class LatLon(val lat: Double, val lon: Double)

fun String.toLatLon(): LatLon {
    val parts = split("|")
    require(parts.size == 2) { "Expected 'lat|lon' format, got: $this" }
    return LatLon(parts[0].toDouble(), parts[1].toDouble())
}
```

- [ ] **Step 6: Create `model/User.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/User.kt
package pl.kokapi.model

data class UserRef(val uuid: String, val username: String)

data class User(
    val uuid: String,
    val username: String,
    val profileUrl: String,
    val cachesFound: Int,
    val hiddenCount: Int,
)
```

- [ ] **Step 7: Create `model/Attribute.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Attribute.kt
package pl.kokapi.model

data class Attribute(
    val acode: String,
    val name: String,
    val isYes: Boolean,
)
```

- [ ] **Step 8: Create `model/Geocache.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Geocache.kt
package pl.kokapi.model

data class Geocache(
    val code: String,
    val name: String,
    val location: LatLon,
    val type: CacheType,
    val status: CacheStatus,
    val difficulty: Float,
    val terrain: Float,
    val owner: UserRef,
    val attributes: List<Attribute>,
    val description: String? = null,
    val hint: String? = null,
    val url: String? = null,
    val founds: Int = 0,
    val notFounds: Int = 0,
    val dateCreated: String? = null,
    val lastFound: String? = null,
)
```

- [ ] **Step 9: Create `model/GeocacheLog.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/model/GeocacheLog.kt
package pl.kokapi.model

data class GeocacheLog(
    val uuid: String,
    val cacheCode: String,
    val user: UserRef,
    val type: LogType,
    val date: String,
    val comment: String,
)
```

- [ ] **Step 10: Create `http/dto/GeocacheDto.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/GeocacheDto.kt
package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocacheDto(
    val code: String,
    val name: String,
    val location: String,
    val type: String,
    val status: String,
    val difficulty: Float = 0f,
    val terrain: Float = 0f,
    val owner: UserRefDto? = null,
    val attributes: List<AttributeDto>? = null,
    val description: String? = null,
    @SerialName("hint2") val hint: String? = null,
    val url: String? = null,
    val founds: Int = 0,
    @SerialName("notfounds") val notFounds: Int = 0,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("last_found") val lastFound: String? = null,
)

@Serializable
internal data class UserRefDto(
    val uuid: String,
    val username: String,
)

@Serializable
internal data class AttributeDto(
    val acode: String,
    val name: String,
)

@Serializable
internal data class SearchResultDto(
    val results: List<String>,
    val more: Boolean,
)

@Serializable
internal data class GeocachesResultDto(
    val results: Map<String, GeocacheDto>,
    val more: Boolean = false,
)
```

- [ ] **Step 11: Create `http/dto/LogDto.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/LogDto.kt
package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocacheLogDto(
    val uuid: String,
    @SerialName("cache_code") val cacheCode: String,
    val user: UserRefDto,
    val type: String,
    val date: String,
    val comment: String = "",
)

@Serializable
internal data class SubmitLogResponseDto(
    val success: Boolean,
    val message: String,
    @SerialName("log_uuid") val logUuid: String? = null,
)
```

- [ ] **Step 12: Create `http/dto/UserDto.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/UserDto.kt
package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDto(
    val uuid: String,
    val username: String,
    @SerialName("profile_url") val profileUrl: String = "",
    @SerialName("caches_found") val cachesFound: Int = 0,
    @SerialName("hidden_count") val hiddenCount: Int = 0,
)
```

- [ ] **Step 13: Create `http/dto/AttributeDto.kt` (full attribute)**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/AttributeDtoFull.kt
package pl.kokapi.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AttributeFullDto(
    val acode: String,
    val name: String,
    val names: Map<String, String> = emptyMap(),
    val description: String? = null,
    @SerialName("is_locally_used") val isLocallyUsed: Boolean = true,
    @SerialName("local_icon_url") val localIconUrl: String? = null,
)
```

- [ ] **Step 14: Create `http/dto/Mappers.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt
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

internal fun AttributeFullDto.toDomain() = pl.kokapi.model.Attribute(
    acode = acode,
    name = name,
    isYes = !acode.endsWith("-0"),
)
```

- [ ] **Step 15: Run mapper tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.model.MappersTest" 2>&1 | tail -20
```
Expected: `3 tests completed, 0 failed`

- [ ] **Step 16: Commit**

```bash
git add kokapi-core/src/commonMain/kotlin/pl/kokapi/
git add kokapi-core/src/commonTest/kotlin/pl/kokapi/model/
git commit -m "feat: add domain models, DTOs, and mappers"
```

---

### Task 3: Error Handling

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/error/KokapiException.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/error/KokapiExceptionTest.kt`

**Interfaces:**
- Produces: `KokapiException` sealed class used by all clients

- [ ] **Step 1: Write failing test**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/error/KokapiExceptionTest.kt
package pl.kokapi.error

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

class KokapiExceptionTest {

    @Test
    fun apiErrorContainsCode() {
        val e = KokapiException.ApiError("invalid_oauth_request", "Bad signature")
        assertEquals("invalid_oauth_request", e.errorCode)
        assertEquals("Bad signature", e.message)
    }

    @Test
    fun httpErrorContainsStatusCode() {
        val e = KokapiException.HttpError(404, "Not Found")
        assertEquals(404, e.code)
    }

    @Test
    fun notAuthenticatedIsKokapiException() {
        val e: KokapiException = KokapiException.NotAuthenticated
        assertIs<KokapiException>(e)
    }

    @Test
    fun exhaustiveWhen() {
        val exceptions: List<KokapiException> = listOf(
            KokapiException.ApiError("x", "y"),
            KokapiException.HttpError(500, "err"),
            KokapiException.NotAuthenticated,
            KokapiException.NetworkError,
            KokapiException.Unknown(null),
        )
        for (e in exceptions) {
            val label = when (e) {
                is KokapiException.ApiError -> "api"
                is KokapiException.HttpError -> "http"
                KokapiException.NotAuthenticated -> "auth"
                KokapiException.NetworkError -> "network"
                is KokapiException.Unknown -> "unknown"
            }
            assert(label.isNotEmpty())
        }
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.error.KokapiExceptionTest" 2>&1 | tail -10
```
Expected: compilation error.

- [ ] **Step 3: Create `KokapiException.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/error/KokapiException.kt
package pl.kokapi.error

sealed class KokapiException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {

    data class ApiError(val errorCode: String, override val message: String) :
        KokapiException(message)

    data class HttpError(val code: Int, val reason: String) :
        KokapiException("HTTP $code: $reason")

    object NotAuthenticated :
        KokapiException("Not authenticated. Call kokapi.auth.login() first.")

    object NetworkError :
        KokapiException("Network unavailable.")

    data class Unknown(override val cause: Throwable?) :
        KokapiException("Unexpected error", cause)
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.error.KokapiExceptionTest" 2>&1 | tail -10
```
Expected: `4 tests completed, 0 failed`

- [ ] **Step 5: Commit**

```bash
git add kokapi-core/src/commonMain/kotlin/pl/kokapi/error/
git add kokapi-core/src/commonTest/kotlin/pl/kokapi/error/
git commit -m "feat: add KokapiException sealed class"
```

---

### Task 4: OAuth Crypto Primitives

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/Base64.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthSigner.kt` (expect)
- Create: `kokapi-core/src/androidMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt` (actual)
- Create: `kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt` (actual)
- Create: `kokapi-core/src/iosMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt` (actual, macOS only)
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/CryptoTest.kt`

**Interfaces:**
- Produces: `internal fun hmacSha1Base64(key: String, data: String): String`, `ByteArray.encodeBase64(): String`, `String.oauthEncode(): String`

- [ ] **Step 1: Write failing crypto tests**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/CryptoTest.kt
package pl.kokapi.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoTest {

    @Test
    fun base64EncodesCorrectly() {
        assertEquals("SGVsbG8=", "Hello".encodeToByteArray().encodeBase64())
        assertEquals("Zm9v", "foo".encodeToByteArray().encodeBase64())
        assertEquals("", ByteArray(0).encodeBase64())
    }

    @Test
    fun oauthEncodeReservedChars() {
        assertEquals("Hello%20World", "Hello World".oauthEncode())
        assertEquals("a%2Bb%3Dc", "a+b=c".oauthEncode())
        assertEquals("abc123-._~", "abc123-._~".oauthEncode())
    }

    @Test
    fun hmacSha1ProducesKnownValue() {
        // Known HMAC-SHA1: key="key", data="The quick brown fox jumps over the lazy dog"
        val result = hmacSha1Base64("key", "The quick brown fox jumps over the lazy dog")
        assertEquals("De7VABMiMTBjEkAOHp2Gj2PZLEQ=", result)
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.CryptoTest" 2>&1 | tail -10
```

- [ ] **Step 3: Create `Base64.kt`** (pure Kotlin, commonMain)

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/Base64.kt
package pl.kokapi.auth

private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

internal fun ByteArray.encodeBase64(): String {
    if (isEmpty()) return ""
    val sb = StringBuilder()
    var i = 0
    while (i < size) {
        val b0 = get(i).toInt() and 0xFF
        val b1 = if (i + 1 < size) get(i + 1).toInt() and 0xFF else 0
        val b2 = if (i + 2 < size) get(i + 2).toInt() and 0xFF else 0
        sb.append(BASE64_CHARS[b0 shr 2])
        sb.append(BASE64_CHARS[(b0 and 0x3) shl 4 or (b1 shr 4)])
        sb.append(if (i + 1 < size) BASE64_CHARS[(b1 and 0xF) shl 2 or (b2 shr 6)] else '=')
        sb.append(if (i + 2 < size) BASE64_CHARS[b2 and 0x3F] else '=')
        i += 3
    }
    return sb.toString()
}

internal fun String.oauthEncode(): String = buildString {
    for (char in this@oauthEncode) {
        when {
            char.isLetterOrDigit() || char == '-' || char == '.' || char == '_' || char == '~' ->
                append(char)
            else -> {
                for (byte in char.toString().encodeToByteArray()) {
                    append('%')
                    append(((byte.toInt() and 0xFF) shr 4).toString(16).uppercase())
                    append((byte.toInt() and 0x0F).toString(16).uppercase())
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create `OAuthSigner.kt`** (expect)

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthSigner.kt
package pl.kokapi.auth

internal expect fun hmacSha1Base64(key: String, data: String): String
```

- [ ] **Step 5: Create JVM actual**

```kotlin
// kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt
package pl.kokapi.auth

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal actual fun hmacSha1Base64(key: String, data: String): String {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(key.encodeToByteArray(), "HmacSHA1"))
    return mac.doFinal(data.encodeToByteArray()).encodeBase64()
}
```

- [ ] **Step 6: Create Android actual** (same impl, different source set)

```kotlin
// kokapi-core/src/androidMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt
package pl.kokapi.auth

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal actual fun hmacSha1Base64(key: String, data: String): String {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(key.encodeToByteArray(), "HmacSHA1"))
    return mac.doFinal(data.encodeToByteArray()).encodeBase64()
}
```

- [ ] **Step 7: Create iOS actual** (only needed on macOS build)

```kotlin
// kokapi-core/src/iosMain/kotlin/pl/kokapi/auth/OAuthSignerImpl.kt
package pl.kokapi.auth

import kotlinx.cinterop.*
import platform.CommonCrypto.*

@OptIn(ExperimentalForeignApi::class)
internal actual fun hmacSha1Base64(key: String, data: String): String {
    val keyBytes = key.encodeToByteArray()
    val dataBytes = data.encodeToByteArray()
    val result = ByteArray(CC_SHA1_DIGEST_LENGTH.toInt())
    keyBytes.usePinned { keyPinned ->
        dataBytes.usePinned { dataPinned ->
            result.usePinned { resultPinned ->
                CCHmac(
                    kCCHmacAlgSHA1,
                    keyPinned.addressOf(0),
                    keyBytes.size.convert(),
                    dataPinned.addressOf(0),
                    dataBytes.size.convert(),
                    resultPinned.addressOf(0)
                )
            }
        }
    }
    return result.encodeBase64()
}
```

- [ ] **Step 8: Run crypto tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.CryptoTest" 2>&1 | tail -10
```
Expected: `3 tests completed, 0 failed`

- [ ] **Step 9: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: add OAuth crypto primitives (HMAC-SHA1, base64, url-encode)"
```

---

### Task 5: Token Storage

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/TokenStorage.kt`
- Create: `kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth/FileTokenStorage.kt`
- Create: `kokapi-core/src/androidMain/kotlin/pl/kokapi/auth/EncryptedPrefsTokenStorage.kt`
- Create: `kokapi-core/src/iosMain/kotlin/pl/kokapi/auth/KeychainTokenStorage.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/TokenStorageTest.kt`

**Interfaces:**
- Produces: `interface TokenStorage { save/get/clear }`, `InMemoryTokenStorage` (commonMain), platform-specific factories

- [ ] **Step 1: Write failing test**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/TokenStorageTest.kt
package pl.kokapi.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenStorageTest {

    @Test
    fun inMemoryStorageSavesAndRetrieves() {
        val storage = InMemoryTokenStorage()
        storage.save("key1", "value1")
        assertEquals("value1", storage.get("key1"))
    }

    @Test
    fun inMemoryStorageReturnsNullForMissingKey() {
        val storage = InMemoryTokenStorage()
        assertNull(storage.get("missing"))
    }

    @Test
    fun inMemoryStorageClears() {
        val storage = InMemoryTokenStorage()
        storage.save("k", "v")
        storage.clear("k")
        assertNull(storage.get("k"))
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.TokenStorageTest" 2>&1 | tail -10
```

- [ ] **Step 3: Create `TokenStorage.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/TokenStorage.kt
package pl.kokapi.auth

interface TokenStorage {
    fun save(key: String, value: String)
    fun get(key: String): String?
    fun clear(key: String)
    fun clearAll()
}

class InMemoryTokenStorage : TokenStorage {
    private val map = mutableMapOf<String, String>()
    override fun save(key: String, value: String) { map[key] = value }
    override fun get(key: String): String? = map[key]
    override fun clear(key: String) { map.remove(key) }
    override fun clearAll() { map.clear() }
}
```

- [ ] **Step 4: Create JVM `FileTokenStorage.kt`**

```kotlin
// kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth/FileTokenStorage.kt
package pl.kokapi.auth

import java.io.File
import java.util.Properties

class FileTokenStorage(dirPath: String = "${System.getProperty("user.home")}/.kokapi") : TokenStorage {
    private val file = File(dirPath).also { it.mkdirs() }.let { File(it, "tokens.properties") }

    private fun load(): Properties = Properties().also {
        if (file.exists()) file.inputStream().use(it::load)
    }

    override fun save(key: String, value: String) {
        val props = load()
        props[key] = value
        file.outputStream().use { props.store(it, null) }
    }

    override fun get(key: String): String? = load().getProperty(key)

    override fun clear(key: String) {
        val props = load()
        props.remove(key)
        file.outputStream().use { props.store(it, null) }
    }

    override fun clearAll() { file.delete() }
}
```

- [ ] **Step 5: Create Android `EncryptedPrefsTokenStorage.kt`**

```kotlin
// kokapi-core/src/androidMain/kotlin/pl/kokapi/auth/EncryptedPrefsTokenStorage.kt
package pl.kokapi.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPrefsTokenStorage(context: Context) : TokenStorage {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "kokapi_tokens", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun save(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun clear(key: String) = prefs.edit().remove(key).apply()
    override fun clearAll() = prefs.edit().clear().apply()
}
```

- [ ] **Step 6: Create iOS `KeychainTokenStorage.kt`**

```kotlin
// kokapi-core/src/iosMain/kotlin/pl/kokapi/auth/KeychainTokenStorage.kt
package pl.kokapi.auth

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class KeychainTokenStorage : TokenStorage {
    private val service = "pl.kokapi"

    override fun save(key: String, value: String) {
        val data = value.encodeToByteArray().toNSData()
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
        )
        SecItemDelete(query)
        val attrs = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecValueData to data,
        )
        SecItemAdd(attrs, null)
    }

    override fun get(key: String): String? {
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val result = memScoped {
            val ref = alloc<CFTypeRefVar>()
            if (SecItemCopyMatching(query, ref.ptr) == errSecSuccess) ref.value else null
        } ?: return null
        return (result as? NSData)?.toByteArray()?.decodeToString()
    }

    override fun clear(key: String) {
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
        )
        SecItemDelete(query)
    }

    override fun clearAll() {
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
        )
        SecItemDelete(query)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CFDictionaryOf(vararg pairs: Pair<CFStringRef?, Any?>): CFDictionaryRef? {
    val keys = pairs.map { it.first }.toCFArray()
    val values = pairs.map { it.second }.toCFArray()
    return CFDictionaryCreate(null, keys, values, pairs.size.convert(), null, null)
}

private fun ByteArray.toNSData(): NSData = NSData.dataWithBytes(this.toCValues(), this.size.convert())

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { arr ->
    arr.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
}
```

- [ ] **Step 7: Run token storage tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.TokenStorageTest" 2>&1 | tail -10
```
Expected: `3 tests completed, 0 failed`

- [ ] **Step 8: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: add TokenStorage interface and platform implementations"
```

---

### Task 6: OAuth 1.0a Manager

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthCredentials.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthManager.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/OAuthManagerTest.kt`

**Interfaces:**
- Consumes: `hmacSha1Base64`, `oauthEncode`, `TokenStorage`, `KokapiSite`
- Produces: `OAuthManager.buildAuthHeader(method, url, params)`, `OAuthManager.requestTokenUrl()`, `OAuthManager.handleCallback(url)`, `OAuthManager.isAuthenticated`

- [ ] **Step 1: Write failing tests**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/auth/OAuthManagerTest.kt
package pl.kokapi.auth

import pl.kokapi.KokapiSite
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class OAuthManagerTest {

    private fun manager(storage: TokenStorage = InMemoryTokenStorage()) = OAuthManager(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "testConsumerKey",
        consumerSecret = "testConsumerSecret",
        callbackUrl = "kokapi://oauth",
        tokenStorage = storage,
    )

    @Test
    fun notAuthenticatedByDefault() {
        assertFalse(manager().isAuthenticated)
    }

    @Test
    fun authHeaderContainsOauthConsumerKey() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/caches/geocache", emptyMap())
        assertTrue(header.contains("oauth_consumer_key=\"testConsumerKey\""), "Header: $header")
    }

    @Test
    fun authHeaderContainsSignature() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/caches/geocache", emptyMap())
        assertTrue(header.contains("oauth_signature="), "Header: $header")
    }

    @Test
    fun authHeaderContainsTimestampAndNonce() {
        val m = manager()
        val header = m.buildAuthHeader("GET", "https://opencaching.pl/okapi/services/test", emptyMap())
        assertTrue(header.contains("oauth_timestamp="))
        assertTrue(header.contains("oauth_nonce="))
    }

    @Test
    fun isAuthenticatedAfterTokensSaved() {
        val storage = InMemoryTokenStorage()
        val m = manager(storage)
        storage.save("oauth_token", "tok123")
        storage.save("oauth_token_secret", "sec456")
        assertTrue(m.isAuthenticated)
    }

    @Test
    fun logoutClearsTokens() {
        val storage = InMemoryTokenStorage()
        val m = manager(storage)
        storage.save("oauth_token", "tok")
        storage.save("oauth_token_secret", "sec")
        m.logout()
        assertFalse(m.isAuthenticated)
    }

    @Test
    fun parseCallbackUrlExtractsVerifier() {
        val m = manager()
        // Simulate storing a request token first
        m.storeRequestToken("reqToken", "reqSecret")
        val verifier = m.parseCallbackUrl("kokapi://oauth?oauth_token=reqToken&oauth_verifier=abc123")
        assertNotNull(verifier)
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.OAuthManagerTest" 2>&1 | tail -10
```

- [ ] **Step 3: Create `OAuthCredentials.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthCredentials.kt
package pl.kokapi.auth

internal data class OAuthCredentials(
    val token: String,
    val tokenSecret: String,
)
```

- [ ] **Step 4: Create `OAuthManager.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/OAuthManager.kt
package pl.kokapi.auth

import pl.kokapi.KokapiSite

private const val KEY_ACCESS_TOKEN = "oauth_token"
private const val KEY_ACCESS_TOKEN_SECRET = "oauth_token_secret"
private const val KEY_REQUEST_TOKEN = "oauth_request_token"
private const val KEY_REQUEST_TOKEN_SECRET = "oauth_request_token_secret"

class OAuthManager(
    private val site: KokapiSite,
    internal val consumerKey: String,
    private val consumerSecret: String,
    private val callbackUrl: String,
    private val tokenStorage: TokenStorage,
) {
    val isAuthenticated: Boolean
        get() = tokenStorage.get(KEY_ACCESS_TOKEN) != null &&
                tokenStorage.get(KEY_ACCESS_TOKEN_SECRET) != null

    internal fun accessCredentials(): OAuthCredentials? {
        val token = tokenStorage.get(KEY_ACCESS_TOKEN) ?: return null
        val secret = tokenStorage.get(KEY_ACCESS_TOKEN_SECRET) ?: return null
        return OAuthCredentials(token, secret)
    }

    internal fun storeRequestToken(token: String, secret: String) {
        tokenStorage.save(KEY_REQUEST_TOKEN, token)
        tokenStorage.save(KEY_REQUEST_TOKEN_SECRET, secret)
    }

    internal fun storeAccessToken(token: String, secret: String) {
        tokenStorage.save(KEY_ACCESS_TOKEN, token)
        tokenStorage.save(KEY_ACCESS_TOKEN_SECRET, secret)
        tokenStorage.clear(KEY_REQUEST_TOKEN)
        tokenStorage.clear(KEY_REQUEST_TOKEN_SECRET)
    }

    fun logout() {
        tokenStorage.clearAll()
    }

    fun getRequestTokenUrl(): String =
        "${site.baseUrl}/oauth/request_token"

    fun getAuthorizeUrl(requestToken: String): String =
        "${site.baseUrl}/oauth/authorize?oauth_token=${requestToken.oauthEncode()}"

    fun getAccessTokenUrl(): String =
        "${site.baseUrl}/oauth/access_token"

    fun parseCallbackUrl(url: String): String? {
        val params = url.substringAfter("?").split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            k to v
        }
        return params["oauth_verifier"]
    }

    fun buildAuthHeader(
        method: String,
        url: String,
        extraParams: Map<String, String> = emptyMap(),
        tokenCredentials: OAuthCredentials? = accessCredentials(),
    ): String {
        val timestamp = (currentTimeMillis() / 1000).toString()
        val nonce = generateNonce()

        val oauthParams = buildMap {
            put("oauth_consumer_key", consumerKey)
            put("oauth_nonce", nonce)
            put("oauth_signature_method", "HMAC-SHA1")
            put("oauth_timestamp", timestamp)
            put("oauth_version", "1.0")
            tokenCredentials?.let { put("oauth_token", it.token) }
        }

        val allParams = (oauthParams + extraParams)
            .entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key.oauthEncode()}=${it.value.oauthEncode()}" }

        val baseUrl = url.substringBefore("?")
        val signatureBase = listOf(
            method.uppercase(),
            baseUrl.oauthEncode(),
            allParams.oauthEncode(),
        ).joinToString("&")

        val signingKey = "${consumerSecret.oauthEncode()}&${(tokenCredentials?.tokenSecret ?: "").oauthEncode()}"
        val signature = hmacSha1Base64(signingKey, signatureBase)

        val headerParams = (oauthParams + ("oauth_signature" to signature))
            .entries
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key.oauthEncode()}=\"${it.value.oauthEncode()}\"" }

        return "OAuth $headerParams"
    }
}

internal expect fun currentTimeMillis(): Long
internal expect fun generateNonce(): String
```

- [ ] **Step 5: Create `currentTimeMillis` + `generateNonce` expect/actual**

```kotlin
// kokapi-core/src/jvmMain/kotlin/pl/kokapi/auth/PlatformUtils.kt
package pl.kokapi.auth

import java.util.UUID

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()
internal actual fun generateNonce(): String = UUID.randomUUID().toString().replace("-", "")
```

```kotlin
// kokapi-core/src/androidMain/kotlin/pl/kokapi/auth/PlatformUtils.kt
package pl.kokapi.auth

import java.util.UUID

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()
internal actual fun generateNonce(): String = UUID.randomUUID().toString().replace("-", "")
```

```kotlin
// kokapi-core/src/iosMain/kotlin/pl/kokapi/auth/PlatformUtils.kt
package pl.kokapi.auth

import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
internal actual fun generateNonce(): String = NSUUID().UUIDString().replace("-", "")
```

Also add expect declarations to `OAuthSigner.kt` or a new `PlatformExpects.kt`:

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/auth/PlatformExpects.kt
package pl.kokapi.auth

internal expect fun currentTimeMillis(): Long
internal expect fun generateNonce(): String
```

Remove duplicate expects from `OAuthManager.kt` (they should only be in `PlatformExpects.kt`).

- [ ] **Step 6: Run OAuth tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.auth.OAuthManagerTest" 2>&1 | tail -15
```
Expected: `7 tests completed, 0 failed`

- [ ] **Step 7: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: add OAuth 1.0a manager with HMAC-SHA1 signing"
```

---

### Task 7: HTTP Engine + KokapiClient Entry Point

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/HttpEngine.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/PlatformHttpClient.kt` (expect)
- Create: `kokapi-core/src/jvmMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt`
- Create: `kokapi-core/src/androidMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt`
- Create: `kokapi-core/src/iosMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt`
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/KokapiClient.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/KokapiClientTest.kt`

**Interfaces:**
- Produces: `KokapiClient(site, consumerKey, consumerSecret, callbackUrl, tokenStorage, httpClient)`, `KokapiClient.caches`, `.logs`, `.users`, `.attrs`, `.replicate`, `.auth`

- [ ] **Step 1: Write failing client test**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/KokapiClientTest.kt
package pl.kokapi

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import pl.kokapi.auth.InMemoryTokenStorage
import kotlin.test.Test
import kotlin.test.assertNotNull

class KokapiClientTest {

    @Test
    fun clientExposesAllEndpointGroups() {
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "key",
            consumerSecret = "secret",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        assertNotNull(client.caches)
        assertNotNull(client.logs)
        assertNotNull(client.users)
        assertNotNull(client.attrs)
        assertNotNull(client.replicate)
        assertNotNull(client.auth)
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.KokapiClientTest" 2>&1 | tail -10
```

- [ ] **Step 3: Create `PlatformHttpClient.kt`** (expect)

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/PlatformHttpClient.kt
package pl.kokapi.http

import io.ktor.client.*

internal expect fun createPlatformHttpClient(): HttpClient
```

- [ ] **Step 4: JVM actual**

```kotlin
// kokapi-core/src/jvmMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt
package pl.kokapi.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

- [ ] **Step 5: Android actual**

```kotlin
// kokapi-core/src/androidMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt
package pl.kokapi.http

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

- [ ] **Step 6: iOS actual**

```kotlin
// kokapi-core/src/iosMain/kotlin/pl/kokapi/http/PlatformHttpClientImpl.kt
package pl.kokapi.http

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

- [ ] **Step 7: Create `HttpEngine.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/http/HttpEngine.kt
package pl.kokapi.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import pl.kokapi.KokapiSite
import pl.kokapi.auth.OAuthManager
import pl.kokapi.error.KokapiException

internal val json = Json { ignoreUnknownKeys = true; isLenient = true }

internal class HttpEngine(
    private val site: KokapiSite,
    private val oauthManager: OAuthManager,
    private val httpClient: HttpClient,
) {
    suspend fun get(path: String, params: Map<String, String> = emptyMap()): String {
        val url = buildUrl(path, params + ("consumer_key" to oauthManager.consumerKey))
        return try {
            val response = httpClient.get(url) {
                header(HttpHeaders.Authorization, oauthManager.buildAuthHeader("GET", url, params))
            }
            handleResponse(response)
        } catch (e: KokapiException) {
            throw e
        } catch (e: Exception) {
            throw KokapiException.NetworkError
        }
    }

    suspend fun post(path: String, params: Map<String, String> = emptyMap()): String {
        val url = "${site.baseUrl}/$path"
        return try {
            val response = httpClient.post(url) {
                header(HttpHeaders.Authorization, oauthManager.buildAuthHeader("POST", url, params))
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(params.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
            handleResponse(response)
        } catch (e: KokapiException) {
            throw e
        } catch (e: Exception) {
            throw KokapiException.NetworkError
        }
    }

    private fun buildUrl(path: String, params: Map<String, String>): String {
        val query = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "${site.baseUrl}/$path?$query"
    }

    private suspend fun handleResponse(response: HttpResponse): String {
        val body = response.bodyAsText()
        return when {
            response.status.isSuccess() -> body
            response.status == HttpStatusCode.Unauthorized ->
                throw KokapiException.NotAuthenticated
            response.status.value in 400..499 -> {
                val errorCode = extractErrorCode(body)
                throw KokapiException.ApiError(errorCode, body)
            }
            else -> throw KokapiException.HttpError(response.status.value, response.status.description)
        }
    }

    private fun extractErrorCode(body: String): String =
        Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: "unknown_error"
}
```

- [ ] **Step 8: Create `KokapiClient.kt`** (stub with endpoint clients as lateinit — they'll be filled in Tasks 9-12)

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/KokapiClient.kt
package pl.kokapi

import io.ktor.client.*
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.auth.OAuthManager
import pl.kokapi.auth.TokenStorage
import pl.kokapi.endpoints.*
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.createPlatformHttpClient

class KokapiClient(
    val site: KokapiSite,
    val consumerKey: String,
    consumerSecret: String = "",
    callbackUrl: String = "oob",
    tokenStorage: TokenStorage = InMemoryTokenStorage(),
    httpClient: HttpClient = createPlatformHttpClient(),
) {
    private val oauthManager = OAuthManager(
        site = site,
        consumerKey = consumerKey,
        consumerSecret = consumerSecret,
        callbackUrl = callbackUrl,
        tokenStorage = tokenStorage,
    )

    private val engine = HttpEngine(site, oauthManager, httpClient)

    val caches: CachesClient = CachesClient(engine)
    val logs: LogsClient = LogsClient(engine)
    val users: UsersClient = UsersClient(engine)
    val attrs: AttrsClient = AttrsClient(engine)
    val replicate: ReplicateClient = ReplicateClient(engine)
    val auth: AuthClient = AuthClient(oauthManager, engine)
}
```

- [ ] **Step 9: Create stub endpoint classes** (so the project compiles)

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt
package pl.kokapi.endpoints
import pl.kokapi.http.HttpEngine
class CachesClient internal constructor(private val engine: HttpEngine)

// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt
package pl.kokapi.endpoints
import pl.kokapi.http.HttpEngine
class LogsClient internal constructor(private val engine: HttpEngine)

// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/UsersClient.kt
package pl.kokapi.endpoints
import pl.kokapi.http.HttpEngine
class UsersClient internal constructor(private val engine: HttpEngine)

// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AttrsClient.kt
package pl.kokapi.endpoints
import pl.kokapi.http.HttpEngine
class AttrsClient internal constructor(private val engine: HttpEngine)

// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/ReplicateClient.kt
package pl.kokapi.endpoints
import pl.kokapi.http.HttpEngine
class ReplicateClient internal constructor(private val engine: HttpEngine)

// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AuthClient.kt
package pl.kokapi.endpoints
import pl.kokapi.auth.OAuthManager
import pl.kokapi.http.HttpEngine
class AuthClient internal constructor(private val oauthManager: OAuthManager, private val engine: HttpEngine)
```

- [ ] **Step 10: Run client test**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.KokapiClientTest" 2>&1 | tail -10
```
Expected: `1 test completed, 0 failed`

- [ ] **Step 11: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: add HttpEngine, KokapiClient, and platform HTTP client factories"
```

---

### Task 8: CachesClient

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CachesClientTest.kt`

**Interfaces:**
- Consumes: `HttpEngine`, `GeocacheDto`, `toDomain()`, `SearchResultDto`
- Produces: `CachesClient.get(code): Geocache`, `CachesClient.search(query, limit, offset): List<Geocache>`, `CachesClient.nearest(lat, lon, limit): List<Geocache>`, `CachesClient.searchFlow(query): Flow<Geocache>`

- [ ] **Step 1: Write failing tests**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CachesClientTest.kt
package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.CacheStatus
import pl.kokapi.model.CacheType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CachesClientTest {

    private fun clientWith(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK) =
        KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine {
                respond(responseBody, status, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        )

    private val geocacheJson = """
        {
          "code": "OP1234",
          "name": "Test Cache",
          "location": "50.1234|20.5678",
          "type": "Traditional",
          "status": "Available",
          "difficulty": 2.0,
          "terrain": 1.5,
          "owner": {"uuid": "u1", "username": "tester"},
          "attributes": []
        }
    """.trimIndent()

    @Test
    fun getCacheById() = runTest {
        val cache = clientWith(geocacheJson).caches.get("OP1234")
        assertEquals("OP1234", cache.code)
        assertEquals("Test Cache", cache.name)
        assertEquals(50.1234, cache.location.lat, 0.0001)
        assertEquals(CacheType.TRADITIONAL, cache.type)
        assertEquals(CacheStatus.AVAILABLE, cache.status)
        assertEquals(2.0f, cache.difficulty)
    }

    @Test
    fun getCacheThrowsApiErrorOn400() = runTest {
        val errorJson = """{"error":"cache_not_found","developer_message":"Cache not found"}"""
        assertFailsWith<KokapiException.ApiError> {
            clientWith(errorJson, HttpStatusCode.BadRequest).caches.get("INVALID")
        }
    }

    @Test
    fun getCacheThrowsNetworkErrorOnException() = runTest {
        val client = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = "k",
            tokenStorage = InMemoryTokenStorage(),
            httpClient = HttpClient(MockEngine { throw RuntimeException("Network down") })
        )
        assertFailsWith<KokapiException.NetworkError> {
            client.caches.get("OP1234")
        }
    }

    private val searchAndRetrieveJson = """
        {
          "results": {
            "OP1": {"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available","difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"},"attributes":[]},
            "OP2": {"code":"OP2","name":"C2","location":"51.1|20.1","type":"Multi","status":"Available","difficulty":2.0,"terrain":2.0,"owner":{"uuid":"u","username":"u"},"attributes":[]}
          },
          "more": false
        }
    """.trimIndent()

    @Test
    fun searchReturnsList() = runTest {
        val results = clientWith(searchAndRetrieveJson).caches.search(
            lat = 51.0, lon = 20.0, radiusKm = 5.0
        )
        assertEquals(2, results.size)
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.endpoints.CachesClientTest" 2>&1 | tail -10
```

- [ ] **Step 3: Implement `CachesClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt
package pl.kokapi.endpoints

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.*
import pl.kokapi.http.json
import pl.kokapi.model.Geocache

class CachesClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "code|name|location|type|status|difficulty|terrain|owner|attributes|description|hint2|url|founds|notfounds|date_created|last_found"

    suspend fun get(code: String): Geocache {
        val body = engine.get("caches/geocache", mapOf(
            "cache_code" to code,
            "fields" to defaultFields,
        ))
        return json.decodeFromString<GeocacheDto>(body).toDomain()
    }

    suspend fun getMultiple(codes: List<String>): List<Geocache> {
        val body = engine.get("caches/geocaches", mapOf(
            "cache_codes" to codes.joinToString("|"),
            "fields" to defaultFields,
        ))
        return json.decodeFromString<Map<String, GeocacheDto>>(body).values.map { it.toDomain() }
    }

    suspend fun search(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        limit: Int = 100,
        offset: Int = 0,
        type: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("center", "$lat|$lon")
            put("radius", radiusKm.toString())
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("fields", defaultFields)
            type?.let { put("type", it) }
        }
        val body = engine.get("caches/search/nearest", params)
        val result = json.decodeFromString<GeocachesResultDto>(body)
        return result.results.values.map { it.toDomain() }
    }

    suspend fun bbox(
        southLat: Double, westLon: Double,
        northLat: Double, eastLon: Double,
        limit: Int = 100,
    ): List<Geocache> {
        val body = engine.get("caches/search/bbox", mapOf(
            "bbox" to "$southLat|$westLon|$northLat|$eastLon",
            "limit" to limit.toString(),
            "fields" to defaultFields,
        ))
        return json.decodeFromString<GeocachesResultDto>(body).results.values.map { it.toDomain() }
    }

    fun searchFlow(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        pageSize: Int = 100,
    ): Flow<Geocache> = flow {
        var offset = 0
        while (true) {
            val page = search(lat, lon, radiusKm, pageSize, offset)
            page.forEach { emit(it) }
            if (page.size < pageSize) break
            offset += pageSize
        }
    }
}
```

- [ ] **Step 4: Run CachesClient tests**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.endpoints.CachesClientTest" 2>&1 | tail -15
```
Expected: `4 tests completed, 0 failed`

- [ ] **Step 5: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: implement CachesClient with get, search, bbox, searchFlow"
```

---

### Task 9: LogsClient + UsersClient + AttrsClient + ReplicateClient + AuthClient

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/UsersClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AttrsClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/ReplicateClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AuthClient.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/LogsClientTest.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/UsersClientTest.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/AttrsClientTest.kt`

**Interfaces:**
- Produces: all remaining endpoint client methods

- [ ] **Step 1: Write failing tests**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/LogsClientTest.kt
package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.LogType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LogsClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) })
    )

    @Test
    fun submitLogSucceeds() = runTest {
        val json = """{"success":true,"message":"Saved","log_uuid":"abc-123"}"""
        val uuid = clientWith(json).logs.submit("OP1234", LogType.FOUND, "Great cache!")
        assertEquals("abc-123", uuid)
    }

    @Test
    fun submitLogThrowsOnFailureResponse() = runTest {
        val json = """{"success":false,"message":"Not allowed","log_uuid":null}"""
        assertFailsWith<KokapiException.ApiError> {
            clientWith(json).logs.submit("OP1234", LogType.FOUND)
        }
    }

    @Test
    fun getLogReturnsGeocacheLog() = runTest {
        val json = """
            {"uuid":"log-1","cache_code":"OP1234","user":{"uuid":"u1","username":"tester"},
             "type":"Found it","date":"2026-10-06","comment":"Found it!"}
        """.trimIndent()
        val log = clientWith(json).logs.get("log-1")
        assertEquals("log-1", log.uuid)
        assertEquals(LogType.FOUND, log.type)
    }
}
```

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/UsersClientTest.kt
package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class UsersClientTest {

    private fun clientWith(body: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) })
    )

    private val userJson = """
        {"uuid":"u1","username":"tester","profile_url":"https://opencaching.pl/user/tester","caches_found":42,"hidden_count":5}
    """.trimIndent()

    @Test
    fun byUsernameReturnsUser() = runTest {
        val user = clientWith(userJson).users.byUsername("tester")
        assertEquals("u1", user.uuid)
        assertEquals("tester", user.username)
        assertEquals(42, user.cachesFound)
    }

    @Test
    fun byUuidReturnsUser() = runTest {
        val user = clientWith(userJson).users.byUuid("u1")
        assertEquals("tester", user.username)
    }
}
```

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/AttrsClientTest.kt
package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttrsClientTest {

    private fun clientWith(body: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) })
    )

    @Test
    fun getAttributeReturnsAttribute() = runTest {
        val json = """{"acode":"A1","name":"Dog friendly","names":{"en":"Dog friendly"}}"""
        val attr = clientWith(json).attrs.attribute("A1")
        assertEquals("A1", attr.acode)
        assertEquals("Dog friendly", attr.name)
        assertTrue(attr.isYes)
    }

    @Test
    fun getAttributesListReturnsList() = runTest {
        val json = """[{"acode":"A1","name":"Dogs"},{"acode":"A2-0","name":"No bikes"}]"""
        val attrs = clientWith(json).attrs.attributes()
        assertEquals(2, attrs.size)
    }
}
```

- [ ] **Step 2: Run to confirm fail**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.endpoints.*" 2>&1 | tail -10
```

- [ ] **Step 3: Implement `LogsClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt
package pl.kokapi.endpoints

import pl.kokapi.error.KokapiException
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.GeocacheLogDto
import pl.kokapi.http.dto.SubmitLogResponseDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogType

class LogsClient internal constructor(private val engine: HttpEngine) {

    suspend fun get(logUuid: String): GeocacheLog {
        val body = engine.get("logs/entry", mapOf(
            "log_uuid" to logUuid,
            "fields" to "uuid|cache_code|user|type|date|comment",
        ))
        return json.decodeFromString<GeocacheLogDto>(body).toDomain()
    }

    suspend fun submit(
        cacheCode: String,
        type: LogType,
        comment: String = "",
        date: String? = null,
        password: String? = null,
    ): String {
        val params = buildMap {
            put("cache_code", cacheCode)
            put("logtype", type.okapiName)
            put("comment", comment)
            date?.let { put("when", it) }
            password?.let { put("password", it) }
        }
        val body = engine.post("logs/submit", params)
        val response = json.decodeFromString<SubmitLogResponseDto>(body)
        if (!response.success) throw KokapiException.ApiError("submit_failed", response.message)
        return response.logUuid ?: throw KokapiException.ApiError("no_uuid", "Log submitted but no UUID returned")
    }

    suspend fun delete(logUuid: String) {
        engine.post("logs/delete", mapOf("log_uuid" to logUuid))
    }
}
```

- [ ] **Step 4: Implement `UsersClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/UsersClient.kt
package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.UserDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.User

class UsersClient internal constructor(private val engine: HttpEngine) {

    private val defaultFields = "uuid|username|profile_url|caches_found|hidden_count"

    suspend fun byUsername(username: String): User {
        val body = engine.get("users/by_username", mapOf(
            "username" to username,
            "fields" to defaultFields,
        ))
        return json.decodeFromString<UserDto>(body).toDomain()
    }

    suspend fun byUuid(uuid: String): User {
        val body = engine.get("users/by_uuid", mapOf(
            "user_uuid" to uuid,
            "fields" to defaultFields,
        ))
        return json.decodeFromString<UserDto>(body).toDomain()
    }

    suspend fun me(): User {
        val body = engine.get("users/user", mapOf(
            "fields" to defaultFields,
        ))
        return json.decodeFromString<UserDto>(body).toDomain()
    }
}
```

- [ ] **Step 5: Implement `AttrsClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AttrsClient.kt
package pl.kokapi.endpoints

import pl.kokapi.http.HttpEngine
import pl.kokapi.http.dto.AttributeDto
import pl.kokapi.http.dto.AttributeFullDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.http.json
import pl.kokapi.model.Attribute

class AttrsClient internal constructor(private val engine: HttpEngine) {

    suspend fun attributes(): List<Attribute> {
        val body = engine.get("attrs/attributes", mapOf("fields" to "acode|name"))
        return json.decodeFromString<List<AttributeDto>>(body).map { it.toDomain() }
    }

    suspend fun attribute(acode: String): Attribute {
        val body = engine.get("attrs/attribute", mapOf(
            "acode" to acode,
            "fields" to "acode|name|names",
        ))
        return json.decodeFromString<AttributeFullDto>(body).toDomain()
    }
}
```

- [ ] **Step 6: Implement `ReplicateClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/ReplicateClient.kt
package pl.kokapi.endpoints

import kotlinx.serialization.Serializable
import pl.kokapi.http.HttpEngine
import pl.kokapi.http.json

@Serializable
data class ChangelogEntry(
    val action: String,
    val cacheCode: String? = null,
    val changeType: String? = null,
)

@Serializable
data class Changelog(
    val revision: Long,
    val more: Boolean,
    val changelog: List<ChangelogEntry>,
)

class ReplicateClient internal constructor(private val engine: HttpEngine) {

    suspend fun changelog(sinceRevision: Long): Changelog {
        val body = engine.get("replicate/changelog", mapOf(
            "since" to sinceRevision.toString(),
        ))
        return json.decodeFromString(body)
    }

    suspend fun currentRevision(): Long {
        val body = engine.get("replicate/changelog", mapOf(
            "since" to "0",
            "limit" to "1",
        ))
        return json.decodeFromString<Changelog>(body).revision
    }
}
```

- [ ] **Step 7: Implement `AuthClient.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AuthClient.kt
package pl.kokapi.endpoints

import pl.kokapi.auth.OAuthManager
import pl.kokapi.http.HttpEngine

data class OAuthLoginStep(
    val authorizeUrl: String,
    internal val requestToken: String,
    internal val requestTokenSecret: String,
)

class AuthClient internal constructor(
    private val oauthManager: OAuthManager,
    private val engine: HttpEngine,
) {
    val isAuthenticated: Boolean get() = oauthManager.isAuthenticated

    suspend fun startLogin(): OAuthLoginStep {
        val response = engine.post("oauth/request_token", mapOf(
            "oauth_callback" to "oob",
        ))
        val params = response.split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            k to v
        }
        val token = params["oauth_token"] ?: error("No oauth_token in response")
        val secret = params["oauth_token_secret"] ?: error("No oauth_token_secret in response")
        oauthManager.storeRequestToken(token, secret)
        return OAuthLoginStep(
            authorizeUrl = oauthManager.getAuthorizeUrl(token),
            requestToken = token,
            requestTokenSecret = secret,
        )
    }

    suspend fun completeLogin(callbackUrl: String): Boolean {
        val verifier = oauthManager.parseCallbackUrl(callbackUrl) ?: return false
        val response = engine.post("oauth/access_token", mapOf("oauth_verifier" to verifier))
        val params = response.split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            k to v
        }
        val token = params["oauth_token"] ?: return false
        val secret = params["oauth_token_secret"] ?: return false
        oauthManager.storeAccessToken(token, secret)
        return true
    }

    fun logout() = oauthManager.logout()
}
```

- [ ] **Step 8: Run all endpoint tests**

```bash
./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```
Expected: all tests pass, 0 failures.

- [ ] **Step 9: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: implement LogsClient, UsersClient, AttrsClient, ReplicateClient, AuthClient"
```

---

### Task 10: Callback Wrappers

**Files:**
- Create: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/Callbacks.kt`
- Test: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CallbacksTest.kt`

**Interfaces:**
- Produces: extension functions `CachesClient.get(code, scope, onSuccess, onFailure)` etc. for iOS/Java interop

- [ ] **Step 1: Write failing test**

```kotlin
// kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CallbacksTest.kt
package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CallbacksTest {

    private val geocacheJson = """
        {"code":"OP1","name":"C1","location":"51.0|20.0","type":"Traditional","status":"Available",
         "difficulty":1.0,"terrain":1.0,"owner":{"uuid":"u","username":"u"},"attributes":[]}
    """.trimIndent()

    private fun kokapi(body: String) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) })
    )

    @Test
    fun callbackSucceeds() = runTest {
        var result: pl.kokapi.model.Geocache? = null
        var error: KokapiException? = null
        val scope = CoroutineScope(Dispatchers.Default)
        kokapi(geocacheJson).caches.get("OP1", scope,
            onSuccess = { result = it },
            onFailure = { error = it }
        )
        // Wait briefly for the coroutine to complete
        kotlinx.coroutines.delay(100)
        assertNotNull(result)
        assertEquals(null, error)
    }
}
```

- [ ] **Step 2: Implement `Callbacks.kt`**

```kotlin
// kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/Callbacks.kt
package pl.kokapi.endpoints

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogType
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
    limit: Int = 100,
    scope: CoroutineScope,
    onSuccess: (List<Geocache>) -> Unit,
    onFailure: (KokapiException) -> Unit,
) = scope.launch {
    runCatching { search(lat, lon, radiusKm, limit) }
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
```

- [ ] **Step 3: Run callback test**

```bash
./gradlew :kokapi-core:desktopTest --tests "pl.kokapi.endpoints.CallbacksTest" 2>&1 | tail -10
```
Expected: `1 test completed, 0 failed`

- [ ] **Step 4: Commit**

```bash
git add kokapi-core/src/
git commit -m "feat: add callback wrappers for iOS/Java interop"
```

---

### Task 11: kokapi-test Module

**Files:**
- Create: `kokapi-test/build.gradle.kts`
- Create: `kokapi-test/src/commonMain/kotlin/pl/kokapi/test/FakeKokapiClient.kt`

**Interfaces:**
- Produces: `FakeKokapiClient` pre-wired with `InMemoryTokenStorage` and `MockEngine` for library consumers

- [ ] **Step 1: Create `kokapi-test/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

val hostOs = System.getProperty("os.name")
val isMacOs = hostOs.startsWith("Mac")

kotlin {
    androidTarget { compilations.all { kotlinOptions { jvmTarget = "1.8" } } }
    jvm("desktop")
    if (isMacOs) {
        iosX64(); iosArm64(); iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kokapi-core"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

android {
    namespace = "pl.kokapi.test"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
}
```

- [ ] **Step 2: Create `FakeKokapiClient.kt`**

```kotlin
// kokapi-test/src/commonMain/kotlin/pl/kokapi/test/FakeKokapiClient.kt
package pl.kokapi.test

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage

class FakeKokapiClient(
    private val responses: MutableMap<String, String> = mutableMapOf(),
    site: KokapiSite = KokapiSite.OPENCACHING_PL,
    consumerKey: String = "fake_key",
) {
    fun respondWith(urlPathContains: String, jsonBody: String) {
        responses[urlPathContains] = jsonBody
    }

    fun build(): KokapiClient = KokapiClient(
        site = site,
        consumerKey = consumerKey,
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine { request ->
            val matchKey = responses.keys.firstOrNull { request.url.toString().contains(it) }
            val body = matchKey?.let { responses[it] } ?: "{}"
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )
}
```

- [ ] **Step 3: Build kokapi-test**

```bash
./gradlew :kokapi-test:desktopJar 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add kokapi-test/
git commit -m "feat: add kokapi-test module with FakeKokapiClient"
```

---

### Task 12: Dokka + README + Docs

**Files:**
- Create: `README.md`
- Create: `docs/getting-started.md`
- Create: `docs/oauth-guide.md`
- Modify: `kokapi-core/build.gradle.kts` (already has dokka plugin)

**Interfaces:**
- Produces: `README.md` with quickstart, generated Dokka HTML at `build/dokka/`

- [ ] **Step 1: Create `README.md`**

```markdown
# KoKAPI

Kotlin Multiplatform library for the [Opencaching OKAPI](https://opencaching.pl/okapi/introduction.html) REST API.

Supports **Android**, **iOS**, and **Desktop (JVM)**.

## Quickstart

### Android

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "YOUR_CONSUMER_KEY",
)

// Search for caches near a location
val caches = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 5.0)
```

### iOS (Swift)

```swift
let kokapi = KokapiClient(
    site: KokapiSite.opencachingPl,
    consumerKey: "YOUR_CONSUMER_KEY",
    consumerSecret: "",
    callbackUrl: "oob",
    tokenStorage: InMemoryTokenStorage(),
    httpClient: /* createPlatformHttpClient() */
)
```

### OAuth Login

```kotlin
val step = kokapi.auth.startLogin()
// Open step.authorizeUrl in a browser
// After user authorizes, handle callback:
val success = kokapi.auth.completeLogin(callbackUrl)
```

## Installation

```kotlin
implementation("pl.kokapi:kokapi-core:1.0.0")
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

## License

MIT
```

- [ ] **Step 2: Create `docs/getting-started.md`**

```markdown
# Getting Started with KoKAPI

## 1. Get a Consumer Key

Register your application at [Opencaching.pl OKAPI](https://opencaching.pl/okapi/introduction.html) to get a `consumer_key` and `consumer_secret`.

## 2. Create a client

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "your_consumer_key",
    consumerSecret = "your_consumer_secret",
    callbackUrl = "yourapp://oauth",
    tokenStorage = FileTokenStorage(), // JVM/Desktop
    // or EncryptedPrefsTokenStorage(context) // Android
)
```

## 3. Fetch geocaches

```kotlin
// Single cache
val cache = kokapi.caches.get("OP1234")
println("${cache.name} at ${cache.location.lat}, ${cache.location.lon}")

// Search nearby
val nearby = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 10.0)
nearby.forEach { println("${it.code}: ${it.name}") }

// Paginated flow
kokapi.caches.searchFlow(lat = 50.0619, lon = 19.9368).collect { cache ->
    println(cache.name)
}
```

## 4. Handle errors

```kotlin
try {
    val cache = kokapi.caches.get("INVALID")
} catch (e: KokapiException.ApiError) {
    println("API error: ${e.errorCode} — ${e.message}")
} catch (e: KokapiException.NetworkError) {
    println("No network")
} catch (e: KokapiException.NotAuthenticated) {
    println("Need to login first")
}
```
```

- [ ] **Step 3: Create `docs/oauth-guide.md`**

```markdown
# OAuth Guide

KoKAPI uses OAuth 1.0a (the same standard as the OKAPI spec requires).

## Flow

### Step 1: Start login

```kotlin
val step = kokapi.auth.startLogin()
// step.authorizeUrl — open this in a browser or WebView
```

### Step 2: User authorizes

Open `step.authorizeUrl` in a browser. The user logs in and approves access.
The browser redirects to your `callbackUrl` with `?oauth_token=...&oauth_verifier=...`.

### Step 3: Complete login

```kotlin
val callbackUrl = "yourapp://oauth?oauth_token=xxx&oauth_verifier=yyy"
val success = kokapi.auth.completeLogin(callbackUrl)
```

Tokens are stored automatically in the `TokenStorage` you provided.

### Step 4: Make authenticated requests

All requests automatically include the stored token. Methods that require Level 3 (like `kokapi.users.me()` or `kokapi.logs.submit(...)`) will work after login.

## Token Storage by Platform

| Platform | Default | How to use |
|---|---|---|
| Android | `EncryptedPrefsTokenStorage` | `KokapiClient(context, ...)` |
| iOS | `KeychainTokenStorage` | `KokapiClient(...)` |
| Desktop/JVM | `FileTokenStorage` | `KokapiClient(...)` |
| All | `InMemoryTokenStorage` | Testing only (not persistent) |
```

- [ ] **Step 4: Generate Dokka docs**

```bash
./gradlew :kokapi-core:dokkaHtml 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL` — docs generated at `kokapi-core/build/dokka/html/`

- [ ] **Step 5: Commit**

```bash
git add README.md docs/
git commit -m "docs: add README, getting started, OAuth guide"
```

---

### Task 13: Example App

**Files:**
- Create: `example-app/androidApp/build.gradle.kts`
- Create: `example-app/androidApp/src/main/AndroidManifest.xml`
- Create: `example-app/androidApp/src/main/kotlin/pl/kokapi/example/android/MainActivity.kt`
- Create: `example-app/desktopApp/build.gradle.kts`
- Create: `example-app/desktopApp/src/main/kotlin/pl/kokapi/example/desktop/Main.kt`

**Interfaces:**
- Produces: runnable Android app + Desktop app demonstrating search, cache detail, and log submit

- [ ] **Step 1: Create `example-app/androidApp/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    androidTarget { compilations.all { kotlinOptions { jvmTarget = "1.8" } } }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":kokapi-core"))
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("androidx.compose.material3:material3:1.3.0")
                implementation("androidx.compose.ui:ui:1.7.3")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
            }
        }
    }
}

android {
    namespace = "pl.kokapi.example.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "pl.kokapi.example.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}
```

- [ ] **Step 2: Create `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:label="KoKAPI Example"
        android:theme="@style/Theme.AppCompat.DayNight">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="kokapi" android:host="oauth" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 3: Create `MainActivity.kt`**

```kotlin
// example-app/androidApp/src/main/kotlin/pl/kokapi/example/android/MainActivity.kt
package pl.kokapi.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.EncryptedPrefsTokenStorage
import pl.kokapi.model.Geocache

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kokapi = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = BuildConfig.CONSUMER_KEY,
            consumerSecret = BuildConfig.CONSUMER_SECRET,
            callbackUrl = "kokapi://oauth",
            tokenStorage = EncryptedPrefsTokenStorage(this),
        )
        setContent {
            MaterialTheme {
                CacheListScreen(kokapi)
            }
        }
    }
}

class CacheViewModel(private val kokapi: KokapiClient) : ViewModel() {
    private val _caches = MutableStateFlow<List<Geocache>>(emptyList())
    val caches: StateFlow<List<Geocache>> = _caches

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun search(lat: Double, lon: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { kokapi.caches.search(lat, lon, radiusKm = 10.0) }
                .onSuccess { _caches.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}

@Composable
fun CacheListScreen(kokapi: KokapiClient) {
    val viewModel = remember { CacheViewModel(kokapi) }
    val caches by viewModel.caches.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.search(lat = 50.0619, lon = 19.9368)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("KoKAPI Example", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        if (loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        LazyColumn {
            items(caches) { cache ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(cache.name, style = MaterialTheme.typography.titleMedium)
                        Text("${cache.code} · ${cache.type} · D${cache.difficulty}/T${cache.terrain}")
                        Text("${cache.location.lat}, ${cache.location.lon}")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create `example-app/desktopApp/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "11" } }
        withJava()
    }
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":kokapi-core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("io.ktor:ktor-client-cio:3.0.1")
            }
        }
    }
}
```

- [ ] **Step 5: Create `Main.kt`**

```kotlin
// example-app/desktopApp/src/main/kotlin/pl/kokapi/example/desktop/Main.kt
package pl.kokapi.example.desktop

import kotlinx.coroutines.runBlocking
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.FileTokenStorage
import pl.kokapi.auth.InMemoryTokenStorage

fun main() = runBlocking {
    val consumerKey = System.getenv("OKAPI_CONSUMER_KEY") ?: run {
        println("Set OKAPI_CONSUMER_KEY env var to run this example")
        return@runBlocking
    }

    val kokapi = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = consumerKey,
        tokenStorage = FileTokenStorage(),
    )

    println("Searching for caches near Krakow...")
    val caches = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 5.0, limit = 10)

    println("Found ${caches.size} caches:")
    caches.forEach { cache ->
        println("  [${cache.code}] ${cache.name} · ${cache.type} · D${cache.difficulty}/T${cache.terrain}")
        println("         ${cache.location.lat}, ${cache.location.lon}")
    }

    if (!kokapi.auth.isAuthenticated) {
        println("\nTo submit logs, login with OAuth:")
        val step = kokapi.auth.startLogin()
        println("Open this URL: ${step.authorizeUrl}")
        print("Enter callback URL after authorization: ")
        val callback = readlnOrNull() ?: return@runBlocking
        val ok = kokapi.auth.completeLogin(callback)
        println(if (ok) "Login successful!" else "Login failed.")
    }
}
```

- [ ] **Step 6: Build desktop app**

```bash
./gradlew :example-app:desktopApp:desktopJar 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add example-app/
git commit -m "feat: add Android and Desktop example apps"
```

---

### Task 14: Final Build + Run All Tests

- [ ] **Step 1: Run full test suite**

```bash
./gradlew :kokapi-core:desktopTest :kokapi-core:testDebugUnitTest 2>&1 | tail -30
```
Expected: all tests pass.

- [ ] **Step 2: Build all modules**

```bash
./gradlew :kokapi-core:jvmJar :kokapi-core:assembleDebug :kokapi-test:desktopJar 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Generate Dokka**

```bash
./gradlew :kokapi-core:dokkaHtml 2>&1 | tail -5
```

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "chore: final build verification — all tests pass"
```
