package pl.kokapi.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import pl.kokapi.KokapiClient
import pl.kokapi.example.screens.*

@Composable
fun App(kokapi: KokapiClient) {
    MaterialTheme {
        var tab by remember { mutableStateOf(0) }
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = {},
                        label = { Text("Caches") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = {},
                        label = { Text("Users") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = {},
                        label = { Text("Attrs") }
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = {},
                        label = { Text("API Info") }
                    )
                    NavigationBarItem(
                        selected = tab == 4,
                        onClick = { tab = 4 },
                        icon = {},
                        label = { Text("Auth") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                when (tab) {
                    0 -> CachesScreen(kokapi)
                    1 -> UsersScreen(kokapi)
                    2 -> AttrsScreen(kokapi)
                    3 -> ApiInfoScreen(kokapi)
                    4 -> AuthScreen(kokapi)
                }
            }
        }
    }
}
