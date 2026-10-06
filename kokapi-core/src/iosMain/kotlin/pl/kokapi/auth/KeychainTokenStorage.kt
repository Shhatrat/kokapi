package pl.kokapi.auth

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class KeychainTokenStorage : TokenStorage {
    private val service = "pl.kokapi"

    override fun save(key: String, value: String) {
        val valueData = value.encodeToByteArray().toNSData()
        val deleteQuery = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
        ).toCFDictionary()
        SecItemDelete(deleteQuery)

        val addQuery = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecValueData to valueData,
        ).toCFDictionary()
        SecItemAdd(addQuery, null)
    }

    override fun get(key: String): String? = memScoped {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        ).toCFDictionary()
        val resultRef = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, resultRef.ptr)
        if (status != errSecSuccess) return@memScoped null
        val data = CFBridgingRelease(resultRef.value) as? NSData ?: return@memScoped null
        data.toByteArray().decodeToString()
    }

    override fun clear(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
        ).toCFDictionary()
        SecItemDelete(query)
    }

    override fun clearAll() {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
        ).toCFDictionary()
        SecItemDelete(query)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef? {
    val keys = keys.toList()
    val values = values.toList()
    return CFDictionaryCreate(
        null,
        keys.toCFArray(),
        values.toCFArray(),
        size.convert(),
        null,
        null
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun List<Any?>.toCFArray(): CPointer<COpaquePointerVar>? = memScoped {
    val arr = allocArray<COpaquePointerVar>(size)
    forEachIndexed { i, v -> arr[i] = v as? CPointer<*> }
    arr
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    NSData.dataWithBytes(toCValues(), size.convert())

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { arr ->
    arr.usePinned { memcpy(it.addressOf(0), bytes, length) }
}
