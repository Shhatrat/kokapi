package pl.kokapi.http

import io.ktor.client.*

internal expect fun createPlatformHttpClient(): HttpClient
