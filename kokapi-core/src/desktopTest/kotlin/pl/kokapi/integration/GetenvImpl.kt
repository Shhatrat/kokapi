package pl.kokapi.integration

internal actual fun getenv(name: String): String? = System.getenv(name)
