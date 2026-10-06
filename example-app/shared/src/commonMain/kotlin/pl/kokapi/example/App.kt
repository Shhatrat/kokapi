package pl.kokapi.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.kokapi.KokapiClient
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Geocache

@Composable
fun App(kokapi: KokapiClient) {
    var caches by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching { kokapi.caches.search(lat = 50.0619, lon = 19.9368, radiusKm = 10.0) }
            .onSuccess { caches = it }
            .onFailure { e ->
                error = when (e) {
                    is KokapiException.NetworkError -> "Network unavailable"
                    is KokapiException.ApiError -> "API error: ${e.errorCode}"
                    else -> e.message ?: "Unknown error"
                }
            }
        loading = false
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("KoKAPI Example", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text("Caches near Kraków: ${caches.size}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(caches) { CacheCard(it) }
                }
            }
        }
    }
}

@Composable
private fun CacheCard(cache: Geocache) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(cache.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${cache.code} · ${cache.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "D${cache.difficulty} / T${cache.terrain} · ${cache.founds} finds",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
