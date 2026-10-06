package pl.kokapi.model

import pl.kokapi.http.dto.GeocacheDto
import pl.kokapi.http.dto.GeocacheLogDto
import pl.kokapi.http.dto.UserDto
import pl.kokapi.http.dto.UserRefDto
import pl.kokapi.http.dto.toDomain
import pl.kokapi.model.`enum`.CacheType
import pl.kokapi.model.`enum`.CacheStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            difficulty = 1f, terrain = 1f,
            owner = UserRefDto("", ""), attributes = emptyList()
        )
        assertEquals(CacheType.OTHER, dto.toDomain().type)
    }

    @Test
    fun latLonParsedFromPipeString() {
        val ll = "51.5074|-0.1278".toLatLon()
        assertEquals(51.5074, ll.lat, 0.0001)
        assertEquals(-0.1278, ll.lon, 0.0001)
    }

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
            latestLogs = emptyList(),
        )
        val cache = dto.toDomain()
        assertEquals(5, cache.willattends)
        assertEquals(12, cache.watchers)
        assertEquals(3, cache.ratingVotes)
        assertEquals(4, cache.myRating)
        assertEquals(true, cache.isNotFound)
        assertEquals(false, cache.isIgnored)
        assertNull(cache.needsMaintenance)
        assertEquals("private note", cache.myNotes)
        assertEquals(0, cache.latestLogs.size)
    }

    @Test
    fun geocacheLogDtoMapsNewFields() {
        val dto = GeocacheLogDto(
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

    @Test
    fun userDtoMapsRcmdFoundsNeeded() {
        val dto = UserDto(
            uuid = "u1", username = "tester",
            profileUrl = "https://oc.pl/u/tester",
            cachesFound = 100, hiddenCount = 5,
            rcmdsGiven = 10, rcmdsLeft = 2,
            rcmdFoundsNeeded = 3,
        )
        val user = dto.toDomain()
        assertEquals(3, user.rcmdFoundsNeeded)
    }
}
