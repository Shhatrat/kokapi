package pl.kokapi.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.EncryptedPrefsTokenStorage
import pl.kokapi.example.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kokapi = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = BuildConfig.CONSUMER_KEY,
            consumerSecret = BuildConfig.CONSUMER_SECRET,
            callbackUrl = "kokapi://oauth",
            tokenStorage = EncryptedPrefsTokenStorage(this),
        )
        setContent { App(kokapi) }
    }
}
