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
