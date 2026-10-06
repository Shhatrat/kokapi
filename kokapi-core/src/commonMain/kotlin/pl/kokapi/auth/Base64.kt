package pl.kokapi.auth

private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

internal fun ByteArray.encodeBase64(): String {
    if (isEmpty()) return ""
    val sb = StringBuilder()
    var i = 0
    while (i < size) {
        val b0 = get(i).toInt() and 0xFF
        val b1 = if (i + 1 < size) get(i + 1).toInt() and 0xFF else 0
        val b2 = if (i + 2 < size) get(i + 2).toInt() and 0xFF else 0
        sb.append(BASE64_CHARS[b0 shr 2])
        sb.append(BASE64_CHARS[(b0 and 0x3) shl 4 or (b1 shr 4)])
        sb.append(if (i + 1 < size) BASE64_CHARS[(b1 and 0xF) shl 2 or (b2 shr 6)] else '=')
        sb.append(if (i + 2 < size) BASE64_CHARS[b2 and 0x3F] else '=')
        i += 3
    }
    return sb.toString()
}

internal fun String.oauthEncode(): String = buildString {
    for (char in this@oauthEncode) {
        when {
            char.isLetterOrDigit() || char == '-' || char == '.' || char == '_' || char == '~' ->
                append(char)
            else -> {
                for (byte in char.toString().encodeToByteArray()) {
                    append('%')
                    append(((byte.toInt() and 0xFF) shr 4).toString(16).uppercase())
                    append((byte.toInt() and 0x0F).toString(16).uppercase())
                }
            }
        }
    }
}
