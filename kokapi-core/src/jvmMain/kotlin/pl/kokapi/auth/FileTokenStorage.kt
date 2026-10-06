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
