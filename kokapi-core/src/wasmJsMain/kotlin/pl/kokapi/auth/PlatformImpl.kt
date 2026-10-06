package pl.kokapi.auth

// Kotlin/Wasm JS interop helpers — must be top-level functions
private fun dateNow(): Double = js("Date.now()")
private fun mathRandom(): Double = js("Math.random()")

internal actual fun hmacSha1Base64(key: String, data: String): String {
    val keyBytes = key.encodeToByteArray()
    val dataBytes = data.encodeToByteArray()
    return hmacSha1(keyBytes, dataBytes).encodeBase64()
}

internal actual fun currentTimeMillis(): Long = dateNow().toLong()

internal actual fun generateNonce(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..32).map { chars[(mathRandom() * 36).toInt()] }.joinToString("")
}

// --- Pure Kotlin SHA-1 ---

private fun Int.rotateLeft(n: Int): Int = (this shl n) or (this ushr (32 - n))

private fun sha1(message: ByteArray): ByteArray {
    var h0 = 0x67452301.toInt()
    var h1 = 0xEFCDAB89.toInt()
    var h2 = 0x98BADCFE.toInt()
    var h3 = 0x10325476.toInt()
    var h4 = 0xC3D2E1F0.toInt()

    val msgLen = message.size
    val bitLen = msgLen.toLong() * 8L

    // Padding
    val padded = mutableListOf<Byte>()
    padded.addAll(message.asList())
    padded.add(0x80.toByte())
    while ((padded.size % 64) != 56) padded.add(0.toByte())
    // Append bit length as 64-bit big-endian
    for (i in 7 downTo 0) padded.add(((bitLen ushr (i * 8)) and 0xFF).toByte())

    val bytes = padded.toByteArray()

    for (chunkStart in bytes.indices step 64) {
        val w = IntArray(80)
        for (i in 0..15) {
            w[i] = ((bytes[chunkStart + i * 4].toInt() and 0xFF) shl 24) or
                   ((bytes[chunkStart + i * 4 + 1].toInt() and 0xFF) shl 16) or
                   ((bytes[chunkStart + i * 4 + 2].toInt() and 0xFF) shl 8) or
                   (bytes[chunkStart + i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 16..79) w[i] = (w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]).rotateLeft(1)

        var a = h0; var b = h1; var c = h2; var d = h3; var e = h4
        for (i in 0..79) {
            val (f, k) = when (i) {
                in 0..19  -> ((b and c) or (b.inv() and d)) to 0x5A827999.toInt()
                in 20..39 -> (b xor c xor d) to 0x6ED9EBA1.toInt()
                in 40..59 -> ((b and c) or (b and d) or (c and d)) to 0x8F1BBCDC.toInt()
                else      -> (b xor c xor d) to 0xCA62C1D6.toInt()
            }
            val temp = a.rotateLeft(5) + f + e + k + w[i]
            e = d; d = c; c = b.rotateLeft(30); b = a; a = temp
        }
        h0 += a; h1 += b; h2 += c; h3 += d; h4 += e
    }

    val digest = ByteArray(20)
    for (i in 0..3) {
        digest[i]      = ((h0 ushr (24 - i * 8)) and 0xFF).toByte()
        digest[i + 4]  = ((h1 ushr (24 - i * 8)) and 0xFF).toByte()
        digest[i + 8]  = ((h2 ushr (24 - i * 8)) and 0xFF).toByte()
        digest[i + 12] = ((h3 ushr (24 - i * 8)) and 0xFF).toByte()
        digest[i + 16] = ((h4 ushr (24 - i * 8)) and 0xFF).toByte()
    }
    return digest
}

private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
    val blockSize = 64
    val actualKey = if (key.size > blockSize) sha1(key) else key
    val paddedKey = actualKey + ByteArray(blockSize - actualKey.size)
    val iPad = ByteArray(blockSize) { (paddedKey[it].toInt() xor 0x36).toByte() }
    val oPad = ByteArray(blockSize) { (paddedKey[it].toInt() xor 0x5C).toByte() }
    return sha1(oPad + sha1(iPad + data))
}
