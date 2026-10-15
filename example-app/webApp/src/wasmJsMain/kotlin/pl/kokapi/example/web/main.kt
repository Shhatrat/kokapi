package pl.kokapi.example.web

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.example.App

private fun getInitialKey(): String = js("window.__OKAPI_CONSUMER_KEY || ''")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        val initialKey = remember { getInitialKey() }
        var consumerKey by remember { mutableStateOf(initialKey) }
        var activeKey by remember { mutableStateOf(initialKey) }

        MaterialTheme {
            if (activeKey.isNotBlank()) {
                val kokapi = remember(activeKey) {
                    KokapiClient(site = KokapiSite.OPENCACHING_PL, consumerKey = activeKey)
                }
                App(kokapi)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(modifier = Modifier.width(360.dp).padding(16.dp)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("KoKAPI Demo", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Enter your Opencaching OKAPI consumer key to continue.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = consumerKey,
                                onValueChange = { consumerKey = it },
                                label = { Text("Consumer Key") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(
                                onClick = { activeKey = consumerKey.trim() },
                                enabled = consumerKey.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start")
                            }
                        }
                    }
                }
            }
        }
    }
}
