package pl.kokapi.example.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.example.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val kokapi = KokapiClient(
        site = KokapiSite.OPENCACHING_PL,
        consumerKey = "OKAPI_KEY_REMOVED",
        consumerSecret = "OKAPI_SECRET_REMOVED",
        callbackUrl = "oob",
    )
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(kokapi)
    }
}
