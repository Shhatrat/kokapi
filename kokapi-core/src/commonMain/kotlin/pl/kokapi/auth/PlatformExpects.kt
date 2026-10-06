package pl.kokapi.auth

internal expect fun hmacSha1Base64(key: String, data: String): String
internal expect fun currentTimeMillis(): Long
internal expect fun generateNonce(): String
