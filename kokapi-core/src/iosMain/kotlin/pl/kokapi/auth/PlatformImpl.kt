package pl.kokapi.auth

import kotlinx.cinterop.*
import platform.CommonCrypto.*
import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970

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

internal actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

internal actual fun generateNonce(): String = NSUUID().UUIDString().replace("-", "")
