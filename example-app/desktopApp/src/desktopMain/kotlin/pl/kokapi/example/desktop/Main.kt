package pl.kokapi.example.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.FileTokenStorage
import pl.kokapi.example.App

fun main() = application {
    val kokapi = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = System.getenv("OKAPI_CONSUMER_KEY") ?: "",
        consumerSecret = System.getenv("OKAPI_CONSUMER_SECRET") ?: "",
        callbackUrl = "kokapi://oauth",
        tokenStorage = FileTokenStorage(),
    )
    Window(onCloseRequest = ::exitApplication, title = "KoKAPI Example") {
        App(kokapi)
    }
}
