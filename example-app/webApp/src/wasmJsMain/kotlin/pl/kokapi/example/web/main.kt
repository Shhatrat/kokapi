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
        consumerKey = js("window.__OKAPI_CONSUMER_KEY || ''") as String,
        consumerSecret = js("window.__OKAPI_CONSUMER_SECRET || ''") as String,
        callbackUrl = "oob",
    )
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(kokapi)
    }
}
