package pl.kokapi.auth

import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal actual fun hmacSha1Base64(key: String, data: String): String {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(key.encodeToByteArray(), "HmacSHA1"))
    return mac.doFinal(data.encodeToByteArray()).encodeBase64()
}

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

internal actual fun generateNonce(): String = UUID.randomUUID().toString().replace("-", "")
