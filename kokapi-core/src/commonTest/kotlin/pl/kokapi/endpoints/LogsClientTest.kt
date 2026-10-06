package pl.kokapi.endpoints

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.InMemoryTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.enum.LogType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogsClientTest {

    private fun clientWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "k",
        tokenStorage = InMemoryTokenStorage(),
        httpClient = HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    private val logJson = """{"uuid":"l1","cache_code":"OP1","user":{"uuid":"u1","username":"user"},"type":"Found it","date":"2024-01-01","comment":"Great!"}"""

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

    @Test
    fun getForCacheReturnsListOfLogs() = runTest {
        val json = """[$logJson]"""
        val logs = clientWith(json).logs.getForCache("OP1")
        assertEquals(1, logs.size)
        assertEquals("l1", logs[0].uuid)
        assertEquals("OP1", logs[0].cacheCode)
        assertEquals(LogType.FOUND, logs[0].type)
    }

    @Test
    fun getUserLogsReturnsListOfLogs() = runTest {
        val json = """[$logJson]"""
        val logs = clientWith(json).logs.getUserLogs("u1")
        assertEquals(1, logs.size)
        assertEquals("l1", logs[0].uuid)
        assertEquals("user", logs[0].user.username)
    }

    @Test
    fun getEntriesReturnsMapOfLogs() = runTest {
        val json = """{"l1":$logJson}"""
        val entries = clientWith(json).logs.getEntries(listOf("l1"))
        assertEquals(1, entries.size)
        assertEquals("l1", entries["l1"]?.uuid)
        assertEquals("OP1", entries["l1"]?.cacheCode)
    }

    @Test
    fun capabilitiesReturnsParsedModel() = runTest {
        val json = """{"can_add":true,"available_log_types":["Found it","DNF"],"requires_password":false,"can_recommend":true,"can_rate":false}"""
        val caps = clientWith(json).logs.capabilities()
        assertTrue(caps.canAdd)
        assertFalse(caps.requiresPassword)
        assertTrue(caps.canRecommend)
        assertFalse(caps.canRate)
        assertEquals(2, caps.availableTypes.size)
    }

    @Test
    fun addImageReturnsImageUuid() = runTest {
        val json = """{"success":true,"message":"ok","image_uuid":"img1"}"""
        val imageUuid = clientWith(json).logs.addImage("log-1", "base64data==")
        assertEquals("img1", imageUuid)
    }

    @Test
    fun addImageThrowsOnFailure() = runTest {
        val json = """{"success":false,"message":"Image too large"}"""
        assertFailsWith<KokapiException.ApiError> {
            clientWith(json).logs.addImage("log-1", "base64data==")
        }
    }

    @Test
    fun submitWithRecommendDoesNotThrow() = runTest {
        val json = """{"success":true,"message":"Saved","log_uuid":"rec-uuid"}"""
        val uuid = clientWith(json).logs.submit("OP1", LogType.FOUND, recommend = true)
        assertEquals("rec-uuid", uuid)
    }

    @Test
    fun editDoesNotThrow() = runTest {
        clientWith("{}").logs.edit("log-1", comment = "Updated comment")
    }

    @Test
    fun deleteDoesNotThrow() = runTest {
        clientWith("{}").logs.delete("log-1")
    }

    @Test
    fun editImageDoesNotThrow() = runTest {
        clientWith("{}").logs.editImage("img-1", caption = "New caption")
    }

    @Test
    fun deleteImageDoesNotThrow() = runTest {
        clientWith("{}").logs.deleteImage("img-1")
    }

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
}
