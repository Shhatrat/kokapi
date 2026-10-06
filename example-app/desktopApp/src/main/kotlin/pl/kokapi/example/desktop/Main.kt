package pl.kokapi.example.desktop

import kotlinx.coroutines.runBlocking
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.FileTokenStorage
import pl.kokapi.error.KokapiException

fun main() = runBlocking {
    val consumerKey = System.getenv("OKAPI_CONSUMER_KEY") ?: run {
        println("Set OKAPI_CONSUMER_KEY environment variable to run this example.")
        println("Register at: https://opencaching.pl/okapi/introduction.html")
        return@runBlocking
    }

    val kokapi = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = consumerKey,
        consumerSecret = System.getenv("OKAPI_CONSUMER_SECRET") ?: "",
        callbackUrl = "kokapi://oauth",
        tokenStorage = FileTokenStorage(),
    )

    println("=== KoKAPI Desktop Example ===")
    println("Searching for caches near Krakow (50.0619, 19.9368)...")

    try {
        val caches = kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 5.0, limit = 10)
        println("Found ${caches.size} caches:\n")
        caches.forEach { cache ->
            println("  [${cache.code}] ${cache.name}")
            println("         Type: ${cache.type}  D${cache.difficulty}/T${cache.terrain}  Status: ${cache.status}")
            println("         Location: ${cache.location.lat}, ${cache.location.lon}")
            println("         Founds: ${cache.founds}")
            println()
        }
    } catch (e: KokapiException.NetworkError) {
        println("Network error — check your internet connection.")
        return@runBlocking
    } catch (e: KokapiException.ApiError) {
        println("API error: ${e.errorCode} — ${e.message}")
        return@runBlocking
    }

    if (!kokapi.auth.isAuthenticated) {
        println("=== OAuth Login (optional) ===")
        println("Login to submit logs and access private data.")
        print("Start OAuth login? [y/N]: ")
        val answer = readlnOrNull()?.trim()?.lowercase()
        if (answer == "y") {
            val step = kokapi.auth.startLogin()
            println("\nOpen this URL in your browser:")
            println(step.authorizeUrl)
            print("\nAfter authorizing, enter the callback URL (kokapi://oauth?...): ")
            val callback = readlnOrNull() ?: return@runBlocking
            val ok = kokapi.auth.completeLogin(callback)
            if (ok) {
                println("Login successful! Token saved.")
                val me = kokapi.users.me()
                println("Logged in as: ${me.username} (${me.cachesFound} finds)")
            } else {
                println("Login failed.")
            }
        }
    } else {
        val me = kokapi.users.me()
        println("Logged in as: ${me.username}")
    }
}
