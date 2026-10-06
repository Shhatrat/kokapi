package pl.kokapi.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoTest {

    @Test
    fun base64EncodesCorrectly() {
        assertEquals("SGVsbG8=", "Hello".encodeToByteArray().encodeBase64())
        assertEquals("Zm9v", "foo".encodeToByteArray().encodeBase64())
        assertEquals("", ByteArray(0).encodeBase64())
    }

    @Test
    fun oauthEncodeReservedChars() {
        assertEquals("Hello%20World", "Hello World".oauthEncode())
        assertEquals("a%2Bb%3Dc", "a+b=c".oauthEncode())
        assertEquals("abc123-._~", "abc123-._~".oauthEncode())
    }

    @Test
    fun hmacSha1ProducesKnownValue() {
        // Known HMAC-SHA1: key="key", data="The quick brown fox jumps over the lazy dog"
        val result = hmacSha1Base64("key", "The quick brown fox jumps over the lazy dog")
        assertEquals("3nybhbi3iqa8ino29wqQcBydtNk=", result)
    }
}
