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
