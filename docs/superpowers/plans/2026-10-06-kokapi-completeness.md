# KoKAPI Completeness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all missing fields, parameters, and functionality in KoKAPI so it fully covers the OKAPI API — models, DTOs, clients, tests, example app, and docs.

**Architecture:** Layer 1 fixes model/DTO/defaultFields gaps (many fields exist in models but are never requested). Layer 2 adds missing search filter parameters. Layer 3 updates the example app. Layer 4 verifies against OKAPI docs and updates missing.md.

**Tech Stack:** Kotlin Multiplatform, Ktor, kotlinx.serialization, MockEngine for tests, Compose Multiplatform for example app.

## Global Constraints

- All code in `kokapi-core/src/commonMain` (platform-agnostic)
- Tests in `kokapi-core/src/commonTest`
- Run tests: `./gradlew :kokapi-core:desktopTest`
- Commit after every task
- No breaking API changes — only additive (new optional fields with defaults)
- All new `Boolean?` fields default to `null`, new `Int` fields default to `0`, new `List` fields default to `emptyList()`

---

## Task 1: Add missing fields to Geocache model + DTO + mapper

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Geocache.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/GeocacheDto.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt`

**Missing fields to add:**
- `Geocache`: `latestLogs: List<GeocacheLog>`, `myNotes: String?`, `needsMaintenance: Boolean?`, `willattends: Int`, `isNotFound: Boolean?`, `isIgnored: Boolean?`, `watchers: Int`, `ratingVotes: Int?`, `myRating: Int?`
- `GeocacheDto`: same fields as snake_case

- [ ] **Step 1: Write failing test for new Geocache fields**

In `MappersTest.kt`, add after existing tests:

```kotlin
@Test
fun geocacheDtoMapsNewFields() {
    val dto = GeocacheDto(
        code = "OP1", name = "X", location = "50.0|20.0",
        type = "Traditional", status = "Available",
        difficulty = 1f, terrain = 1f,
        owner = UserRefDto("u", "u"), attributes = emptyList(),
        willattends = 5,
        watchers = 12,
        ratingVotes = 3,
        myRating = 4,
        isNotFound = true,
        isIgnored = false,
        needsMaintenance = null,
        myNotes = "private note",
    )
    val cache = dto.toDomain()
    assertEquals(5, cache.willattends)
    assertEquals(12, cache.watchers)
    assertEquals(3, cache.ratingVotes)
    assertEquals(4, cache.myRating)
    assertEquals(true, cache.isNotFound)
    assertEquals(false, cache.isIgnored)
    assertEquals(null, cache.needsMaintenance)
    assertEquals("private note", cache.myNotes)
}
```

- [ ] **Step 2: Run test — expect compile error (fields don't exist)**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 3: Add fields to GeocacheDto**

In `GeocacheDto.kt`, add after `isWatched`:

```kotlin
    @SerialName("is_not_found") val isNotFound: Boolean? = null,
    @SerialName("is_ignored") val isIgnored: Boolean? = null,
    val watchers: Int = 0,
    @SerialName("rating_votes") val ratingVotes: Int? = null,
    @SerialName("my_rating") val myRating: Int? = null,
    val willattends: Int = 0,
    @SerialName("needs_maintenance") val needsMaintenance: Boolean? = null,
    @SerialName("my_notes") val myNotes: String? = null,
    @SerialName("latest_logs") val latestLogs: List<GeocacheLogDto>? = null,
```

- [ ] **Step 4: Add fields to Geocache model**

In `Geocache.kt`, add after `isWatched`:

```kotlin
    val isNotFound: Boolean? = null,
    val isIgnored: Boolean? = null,
    val watchers: Int = 0,
    val ratingVotes: Int? = null,
    val myRating: Int? = null,
    val willattends: Int = 0,
    val needsMaintenance: Boolean? = null,
    val myNotes: String? = null,
    val latestLogs: List<GeocacheLog> = emptyList(),
```

- [ ] **Step 5: Update GeocacheDto.toDomain() in Mappers.kt**

Add after `isWatched = isWatched,`:

```kotlin
    isNotFound = isNotFound,
    isIgnored = isIgnored,
    watchers = watchers,
    ratingVotes = ratingVotes,
    myRating = myRating,
    willattends = willattends,
    needsMaintenance = needsMaintenance,
    myNotes = myNotes,
    latestLogs = latestLogs?.map { it.toDomain() } ?: emptyList(),
```

- [ ] **Step 6: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/model/Geocache.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/GeocacheDto.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt && git commit -m "feat: add missing Geocache fields (latestLogs, myNotes, willattends, watchers, etc.)"
```

---

## Task 2: Add missing fields to GeocacheLog model + DTO + mapper

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/GeocacheLog.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/LogDto.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt`

**Missing fields:** `location: String?`, `ocTeamEntry: Boolean`

- [ ] **Step 1: Write failing test**

In `MappersTest.kt`, add:

```kotlin
@Test
fun geocacheLogDtoMapsNewFields() {
    val dto = pl.kokapi.http.dto.GeocacheLogDto(
        uuid = "l1", cacheCode = "OP1",
        user = UserRefDto("u", "u"),
        type = "Found it", date = "2026-01-01", comment = "",
        location = "50.1|20.2",
        ocTeamEntry = true,
    )
    val log = dto.toDomain()
    assertEquals("50.1|20.2", log.location)
    assertEquals(true, log.ocTeamEntry)
}
```

- [ ] **Step 2: Run test — expect compile error**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 3: Add fields to GeocacheLogDto**

In `LogDto.kt`, add after `lastModified`:

```kotlin
    val location: String? = null,
    @SerialName("oc_team_entry") val ocTeamEntry: Boolean = false,
```

- [ ] **Step 4: Add fields to GeocacheLog model**

In `GeocacheLog.kt`, add after `lastModified`:

```kotlin
    val location: String? = null,
    val ocTeamEntry: Boolean = false,
```

- [ ] **Step 5: Update GeocacheLogDto.toDomain() in Mappers.kt**

Add after `lastModified = lastModified,`:

```kotlin
    location = location,
    ocTeamEntry = ocTeamEntry,
```

- [ ] **Step 6: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 7: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/model/GeocacheLog.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/LogDto.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt && git commit -m "feat: add location and ocTeamEntry to GeocacheLog"
```

---

## Task 3: Add rcmdFoundsNeeded to User model + DTO + mapper

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/model/User.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/UserDto.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt`

- [ ] **Step 1: Write failing test**

In `MappersTest.kt`, add:

```kotlin
@Test
fun userDtoMapsRcmdFoundsNeeded() {
    val dto = pl.kokapi.http.dto.UserDto(
        uuid = "u1", username = "tester",
        profileUrl = "https://oc.pl/u/tester",
        cachesFound = 100, hiddenCount = 5,
        rcmdsGiven = 10, rcmdsLeft = 2,
        rcmdFoundsNeeded = 3,
    )
    val user = dto.toDomain()
    assertEquals(3, user.rcmdFoundsNeeded)
}
```

- [ ] **Step 2: Run test — expect compile error**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 3: Add field to UserDto**

In `UserDto.kt`, add after `rcmdsLeft`:

```kotlin
    @SerialName("rcmd_founds_needed") val rcmdFoundsNeeded: Int = 0,
```

- [ ] **Step 4: Add field to User model**

In `User.kt`, add after `rcmdsLeft`:

```kotlin
    val rcmdFoundsNeeded: Int = 0,
```

- [ ] **Step 5: Update UserDto.toDomain() in Mappers.kt**

Add after `rcmdsLeft = rcmdsLeft,`:

```kotlin
    rcmdFoundsNeeded = rcmdFoundsNeeded,
```

- [ ] **Step 6: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 7: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/model/User.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/UserDto.kt kokapi-core/src/commonMain/kotlin/pl/kokapi/http/dto/Mappers.kt kokapi-core/src/commonTest/kotlin/pl/kokapi/model/MappersTest.kt && git commit -m "feat: add rcmdFoundsNeeded to User"
```

---

## Task 4: Fix defaultFields in all clients (bug: many model fields never requested)

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/UsersClient.kt`
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/AttrsClient.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CachesClientTest.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/LogsClientTest.kt`

**Context:** The current `defaultFields` strings are too short. Fields like `gc_code`, `size2`, `images`, `is_found`, etc. are in the model/DTO but never populated because they're not requested from OKAPI.

- [ ] **Step 1: Write failing tests that verify new fields are populated**

In `CachesClientTest.kt`, add:

```kotlin
@Test
fun getCachePopulatesExtendedFields() = runTest {
    val json = """
        {
          "code":"OP1","name":"X","location":"50.0|20.0","type":"Traditional",
          "status":"Available","difficulty":1.0,"terrain":1.0,
          "owner":{"uuid":"u","username":"u"},
          "gc_code":"GC1234","size2":"regular","recommendations":3,
          "req_passwd":true,"willattends":2,"watchers":7,
          "is_found":true,"is_not_found":false,"is_watched":true,"is_ignored":false,
          "rating":4.5,"rating_votes":10,"my_rating":5,
          "my_notes":"secret","needs_maintenance":null,
          "latest_logs":[]
        }
    """.trimIndent()
    val cache = clientWith(json).caches.get("OP1")
    assertEquals("GC1234", cache.gcCode)
    assertEquals("regular", cache.size2)
    assertEquals(3, cache.recommendations)
    assertEquals(true, cache.reqPasswd)
    assertEquals(2, cache.willattends)
    assertEquals(7, cache.watchers)
    assertEquals(true, cache.isFound)
    assertEquals(false, cache.isNotFound)
    assertEquals(true, cache.isWatched)
    assertEquals(false, cache.isIgnored)
    assertEquals(4.5f, cache.rating)
    assertEquals(10, cache.ratingVotes)
    assertEquals(5, cache.myRating)
    assertEquals("secret", cache.myNotes)
}
```

In `LogsClientTest.kt`, add:

```kotlin
@Test
fun getLogPopulatesExtendedFields() = runTest {
    val json = """
        {"uuid":"l1","cache_code":"OP1","user":{"uuid":"u","username":"u"},
         "type":"Found it","date":"2026-01-01","comment":"Hi",
         "location":"50.1|20.2","oc_team_entry":true,
         "was_recommended":true,"needs_maintenance2":false}
    """.trimIndent()
    val log = clientWith(json).logs.get("l1")
    assertEquals("50.1|20.2", log.location)
    assertEquals(true, log.ocTeamEntry)
    assertEquals(true, log.wasRecommended)
    assertEquals(false, log.needsMaintenance)
}
```

- [ ] **Step 2: Run tests — expect PASS (fields exist, just not populated from request)**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

These tests should already pass since the mapping exists. If not, fix the mapper.

- [ ] **Step 3: Fix CachesClient defaultFields**

In `CachesClient.kt`, replace:

```kotlin
    private val defaultFields =
        "code|name|location|type|status|difficulty|terrain|owner|description|hint2|url|founds|notfounds|date_created|last_found"
```

with:

```kotlin
    private val defaultFields =
        "code|name|location|type|status|difficulty|terrain|owner|attributes|description|hint2|url|" +
        "founds|notfounds|date_created|last_found|gc_code|size2|recommendations|req_passwd|" +
        "short_description|images|trackables_count|trackables|alt_wpts|country2|region|rating|" +
        "trip_time|trip_distance|date_hidden|last_modified|is_found|is_not_found|is_watched|" +
        "is_ignored|willattends|watchers|rating_votes|my_rating|needs_maintenance|my_notes|" +
        "latest_logs"
```

- [ ] **Step 4: Fix LogsClient defaultFields**

In `LogsClient.kt`, replace:

```kotlin
    private val defaultFields = "uuid|cache_code|user|type|date|comment"
```

with:

```kotlin
    private val defaultFields = "uuid|cache_code|user|type|date|comment|images|was_recommended|needs_maintenance2|date_created|last_modified|location|oc_team_entry"
```

- [ ] **Step 5: Fix UsersClient defaultFields**

In `UsersClient.kt`, replace:

```kotlin
    private val defaultFields = "uuid|username|profile_url|caches_found|caches_hidden|rcmds_given|rcmds_left|home_location"
```

with:

```kotlin
    private val defaultFields = "uuid|username|profile_url|caches_found|caches_hidden|rcmds_given|rcmds_left|rcmd_founds_needed|home_location"
```

- [ ] **Step 6: Fix AttrsClient attributes() fields**

In `AttrsClient.kt`, replace in `attributes()`:

```kotlin
                "fields" to "acode|name",
```

with:

```kotlin
                "fields" to "acode|name|local_icon_url|description",
```

Also fix `attribute()` to request description:

```kotlin
                "fields" to "acode|name|names|local_icon_url|description",
```

- [ ] **Step 7: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/ kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/ && git commit -m "fix: request all available fields in defaultFields (gc_code, size2, images, is_found, etc. were never requested)"
```

---

## Task 5: Add missing search filter parameters to search(), bbox(), searchAll(), searchSave()

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CachesClientTest.kt`

**Missing filters to add to `search()` and `bbox()`** (they already exist in `searchAll()`): `ownerUuid`, `name`, `size2`, `minRcmds`, `minFounds`, `maxFounds`, `modifiedSince`, `excludeMyOwn`, `withTrackablesOnly`, `foundBy`, `notFoundBy`, `recommendedOnly`, `recommendedBy`, `watchedOnly`, `ignoredStatus`, `ftfHunter`, `setAnd`.

**Missing filters to add to `searchAll()` and `searchSave()`**: `foundBy`, `notFoundBy`, `recommendedOnly`, `recommendedBy`, `watchedOnly`, `ignoredStatus`, `ftfHunter`, `setAnd`, `rating`.

- [ ] **Step 1: Write tests for new search filter params**

In `CachesClientTest.kt`, add:

```kotlin
@Test
fun searchWithNewFiltersDoesNotThrow() = runTest {
    val searchCodesJson = """{"results":[],"more":false}"""
    val client = clientWith(searchCodesJson)
    val results = client.caches.search(
        lat = 51.0, lon = 20.0,
        ownerUuid = "uuid-owner",
        foundBy = "uuid-found",
        recommendedOnly = true,
        excludeMyOwn = true,
        watchedOnly = true,
    )
    assertEquals(0, results.size)
}

@Test
fun bboxWithNewFiltersDoesNotThrow() = runTest {
    val client = clientWith("""{"results":[],"more":false}""")
    val results = client.caches.bbox(
        southLat = 50.0, westLon = 19.0,
        northLat = 51.0, eastLon = 21.0,
        foundBy = "uuid-found",
        recommendedOnly = true,
    )
    assertEquals(0, results.size)
}

@Test
fun searchAllWithNewFiltersDoesNotThrow() = runTest {
    val client = clientWith("""{"results":[],"more":false}""")
    val results = client.caches.searchAll(
        foundBy = "uuid-found",
        notFoundBy = "uuid-notfound",
        recommendedOnly = true,
        recommendedBy = "uuid-rcmd",
        watchedOnly = true,
        ignoredStatus = "unignored_only",
        ftfHunter = true,
        setAnd = "set123",
        rating = "3-5",
    )
    assertEquals(0, results.size)
}
```

- [ ] **Step 2: Run tests — expect compile error (params don't exist)**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 3: Update search() signature and implementation**

In `CachesClient.kt`, replace `search()` function with:

```kotlin
    suspend fun search(
        lat: Double,
        lon: Double,
        radiusKm: Double = 10.0,
        limit: Int = 100,
        offset: Int = 0,
        type: String? = null,
        status: String = "Available",
        difficulty: String? = null,
        terrain: String? = null,
        size2: List<String>? = null,
        ownerUuid: String? = null,
        name: String? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("center", "$lat|$lon")
            put("radius", radiusKm.toString())
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("status", status)
            type?.let { put("type", it) }
            difficulty?.let { put("difficulty", it) }
            terrain?.let { put("terrain", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it) }
            name?.let { put("name", it) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/nearest", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }
```

- [ ] **Step 4: Update bbox() signature and implementation**

Replace `bbox()` function with:

```kotlin
    suspend fun bbox(
        southLat: Double,
        westLon: Double,
        northLat: Double,
        eastLon: Double,
        limit: Int = 100,
        offset: Int = 0,
        type: String? = null,
        status: String = "Available",
        difficulty: String? = null,
        terrain: String? = null,
        size2: List<String>? = null,
        ownerUuid: String? = null,
        name: String? = null,
        minRcmds: String? = null,
        minFounds: Int? = null,
        maxFounds: Int? = null,
        modifiedSince: String? = null,
        foundStatus: String? = null,
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        excludeMyOwn: Boolean = false,
        withTrackablesOnly: Boolean = false,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
        orderBy: String? = null,
    ): List<Geocache> {
        val params = buildMap {
            put("bbox", "$southLat|$westLon|$northLat|$eastLon")
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("status", status)
            type?.let { put("type", it) }
            difficulty?.let { put("difficulty", it) }
            terrain?.let { put("terrain", it) }
            size2?.let { put("size2", it.joinToString("|")) }
            ownerUuid?.let { put("owner_uuid", it) }
            name?.let { put("name", it) }
            minRcmds?.let { put("min_rcmds", it) }
            minFounds?.let { put("min_founds", it.toString()) }
            maxFounds?.let { put("max_founds", it.toString()) }
            modifiedSince?.let { put("modified_since", it) }
            foundStatus?.let { put("found_status", it) }
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (excludeMyOwn) put("exclude_my_own", "true")
            if (withTrackablesOnly) put("with_trackables_only", "true")
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
            orderBy?.let { put("order_by", it) }
        }
        val body = engine.get("caches/search/bbox", params)
        val codes = json.decodeFromString<SearchResultDto>(body).results
        if (codes.isEmpty()) return emptyList()
        return getMultiple(codes)
    }
```

- [ ] **Step 5: Update searchAll() and searchSave() with missing filters**

In `searchAll()`, add to the parameter list after `orderBy`:

```kotlin
        foundBy: String? = null,
        notFoundBy: String? = null,
        recommendedOnly: Boolean = false,
        recommendedBy: String? = null,
        watchedOnly: Boolean = false,
        ignoredStatus: String? = null,
        ftfHunter: Boolean = false,
        setAnd: String? = null,
        rating: String? = null,
```

And in the `buildMap` block, add:

```kotlin
            foundBy?.let { put("found_by", it) }
            notFoundBy?.let { put("not_found_by", it) }
            if (recommendedOnly) put("recommended_only", "true")
            recommendedBy?.let { put("recommended_by", it) }
            if (watchedOnly) put("watched_only", "true")
            ignoredStatus?.let { put("ignored_status", it) }
            if (ftfHunter) put("ftf_hunter", "true")
            setAnd?.let { put("set_and", it) }
            rating?.let { put("rating", it) }
```

Apply the same additions to `searchSave()`.

- [ ] **Step 6: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 7: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/CachesClient.kt kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/CachesClientTest.kt && git commit -m "feat: add missing search filters (foundBy, recommendedOnly, watchedOnly, ftfHunter, etc.) to search/bbox/searchAll/searchSave"
```

---

## Task 6: Add on_duplicate parameter to LogsClient.submit()

**Files:**
- Modify: `kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt`
- Modify: `kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/LogsClientTest.kt`

- [ ] **Step 1: Write test**

In `LogsClientTest.kt`, add:

```kotlin
@Test
fun submitLogWithOnDuplicateDoesNotThrow() = runTest {
    val json = """{"success":true,"message":"Saved","log_uuid":"abc-123"}"""
    val uuid = clientWith(json).logs.submit(
        cacheCode = "OP1",
        type = LogType.FOUND,
        comment = "Great!",
        onDuplicate = "continue",
    )
    assertEquals("abc-123", uuid)
}
```

- [ ] **Step 2: Run test — expect compile error**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 3: Add onDuplicate parameter to submit()**

In `LogsClient.kt`, add `onDuplicate: String = "silent_success"` to `submit()` parameters, and in the `buildMap` block add:

```kotlin
            put("on_duplicate", onDuplicate)
```

- [ ] **Step 4: Run tests — expect PASS**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
cd /home/szymon/programming/kokapi && git add kokapi-core/src/commonMain/kotlin/pl/kokapi/endpoints/LogsClient.kt kokapi-core/src/commonTest/kotlin/pl/kokapi/endpoints/LogsClientTest.kt && git commit -m "feat: add on_duplicate parameter to logs/submit"
```

---

## Task 7: Update example app — CachesScreen

**Files:**
- Modify: `example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/CachesScreen.kt`

Show new fields in the cache detail display: `willattends`, `watchers`, `isNotFound`, `isIgnored`, `ratingVotes`, `myRating`, `needsMaintenance`, `myNotes`, `latestLogs` count. Add new filter params to search examples.

- [ ] **Step 1: Find where cache details are displayed**

```bash
grep -n "isFound\|isWatched\|rating\|gcCode" /home/szymon/programming/kokapi/example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/CachesScreen.kt | head -20
```

- [ ] **Step 2: Add new fields to cache display section**

Find the section that displays `isFound`, `isWatched`, `rating` etc., and add after it:

```kotlin
if (cache.willattends > 0) Text("Will attend: ${cache.willattends}")
if (cache.watchers > 0) Text("Watchers: ${cache.watchers}")
cache.isNotFound?.let { Text("Is not found by me: $it") }
cache.isIgnored?.let { Text("Is ignored: $it") }
cache.ratingVotes?.let { Text("Rating votes: $it") }
cache.myRating?.let { Text("My rating: $it/5") }
cache.needsMaintenance?.let { Text("Needs maintenance: $it") }
cache.myNotes?.let { Text("My notes: $it") }
if (cache.latestLogs.isNotEmpty()) Text("Latest logs: ${cache.latestLogs.size}")
```

- [ ] **Step 3: Add new filter params to search example**

Find the search call in CachesScreen and add an example of new filters with a UI toggle (e.g. "Recommended only" checkbox or button).

- [ ] **Step 4: Commit**

```bash
cd /home/szymon/programming/kokapi && git add example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/CachesScreen.kt && git commit -m "feat(example): show new Geocache fields and search filters in CachesScreen"
```

---

## Task 8: Update example app — UsersScreen and AttrsScreen

**Files:**
- Modify: `example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/UsersScreen.kt`
- Modify: `example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/AttrsScreen.kt`

- [ ] **Step 1: Add rcmdFoundsNeeded to UsersScreen**

Find where `rcmdsGiven` and `rcmdsLeft` are displayed in `UsersScreen.kt` and add:

```kotlin
if (user.rcmdFoundsNeeded > 0) Text("Finds needed for next rcmd: ${user.rcmdFoundsNeeded}")
```

- [ ] **Step 2: Verify attrs show iconUrl and description**

Check if `AttrsScreen.kt` displays `iconUrl` and `description`. If not, add them where attribute details are displayed.

- [ ] **Step 3: Commit**

```bash
cd /home/szymon/programming/kokapi && git add example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/UsersScreen.kt example-app/shared/src/commonMain/kotlin/pl/kokapi/example/screens/AttrsScreen.kt && git commit -m "feat(example): show rcmdFoundsNeeded in UsersScreen, iconUrl/description in AttrsScreen"
```

---

## Task 9: Update missing.md to reflect current state

**Files:**
- Modify: `missing.md`

- [ ] **Step 1: Rewrite missing.md**

Update `missing.md` to accurately reflect what's still missing vs. what's now implemented. Mark implemented items clearly. Remaining gaps after this plan: OCPL-specific fields (`powertrail_only`), `protection_areas`, `attribution_note`, `internal_id` in Geocache that aren't yet in the model.

- [ ] **Step 2: Commit**

```bash
cd /home/szymon/programming/kokapi && git add missing.md && git commit -m "docs: update missing.md to reflect current implementation status"
```

---

## Task 10: Final verification — run all tests

- [ ] **Step 1: Run full test suite**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | tail -30
```

Expected: all tests pass, `BUILD SUCCESSFUL`

- [ ] **Step 2: Check test count**

```bash
cd /home/szymon/programming/kokapi && ./gradlew :kokapi-core:desktopTest 2>&1 | grep "tests\|passed\|failed"
```

- [ ] **Step 3: Build example desktop app**

```bash
sshpass -p "13" ssh szymon@192.168.0.21 "cd /home/szymon/programming/kokapi && ./gradlew :example-app:desktopApp:compileKotlinDesktop 2>&1 | tail -20"
```

- [ ] **Step 4: Final commit if any stray changes**

```bash
cd /home/szymon/programming/kokapi && git status
```
