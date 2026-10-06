package pl.kokapi.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.kokapi.KokapiClient
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog

@Composable
fun CachesScreen(kokapi: KokapiClient) {
    var useBbox by remember { mutableStateOf(false) }
    var caches by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCache by remember { mutableStateOf<Geocache?>(null) }
    val scope = rememberCoroutineScope()

    // Nearest search params
    var lat by remember { mutableStateOf("50.0619") }
    var lon by remember { mutableStateOf("19.9368") }
    var radius by remember { mutableStateOf("10.0") }

    // BBox params
    var southLat by remember { mutableStateOf("50.0") }
    var westLon by remember { mutableStateOf("19.8") }
    var northLat by remember { mutableStateOf("50.1") }
    var eastLon by remember { mutableStateOf("20.0") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Caches", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("BBox search")
            Switch(checked = useBbox, onCheckedChange = {
                useBbox = it
                selectedCache = null
                caches = emptyList()
                error = null
            })
        }

        Spacer(Modifier.height(8.dp))

        if (!useBbox) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Lon") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius km") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = southLat,
                    onValueChange = { southLat = it },
                    label = { Text("S lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = westLon,
                    onValueChange = { westLon = it },
                    label = { Text("W lon") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = northLat,
                    onValueChange = { northLat = it },
                    label = { Text("N lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = eastLon,
                    onValueChange = { eastLon = it },
                    label = { Text("E lon") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            scope.launch {
                loading = true
                error = null
                selectedCache = null
                runCatching {
                    if (!useBbox) {
                        kokapi.caches.search(
                            lat = lat.toDouble(),
                            lon = lon.toDouble(),
                            radiusKm = radius.toDouble()
                        )
                    } else {
                        kokapi.caches.bbox(
                            southLat = southLat.toDouble(),
                            westLon = westLon.toDouble(),
                            northLat = northLat.toDouble(),
                            eastLon = eastLon.toDouble()
                        )
                    }
                }.onSuccess { caches = it }
                    .onFailure { error = it.message ?: "Unknown error" }
                loading = false
            }
        }) { Text("Search") }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (selectedCache != null) {
            Spacer(Modifier.height(8.dp))
            CacheDetail(
                cache = selectedCache!!,
                kokapi = kokapi,
                onBack = { selectedCache = null }
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text("Results: ${caches.size}", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(caches) { cache ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedCache = cache }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(cache.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${cache.code} · ${cache.type.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "D${cache.difficulty} / T${cache.terrain}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CacheDetail(cache: Geocache, kokapi: KokapiClient, onBack: () -> Unit) {
    var logs by remember { mutableStateOf<List<GeocacheLog>>(emptyList()) }
    var logsLoading by remember { mutableStateOf(false) }
    var logsError by remember { mutableStateOf<String?>(null) }
    var gpx by remember { mutableStateOf<String?>(null) }
    var gpxLoading by remember { mutableStateOf(false) }
    var gpxError by remember { mutableStateOf<String?>(null) }
    var markResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBack) { Text("Back to list") }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(cache.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text("Code: ${cache.code}")
                Text("Type: ${cache.type.name}")
                Text("Status: ${cache.status.name}")
                Text("Difficulty: ${cache.difficulty} / Terrain: ${cache.terrain}")
                Text("Owner: ${cache.owner.username}")
                Text("Founds: ${cache.founds}")
                cache.description?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Description:", style = MaterialTheme.typography.labelMedium)
                    Text(it.take(300) + if (it.length > 300) "..." else "")
                }
                cache.hint?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Hint: $it")
                }
                cache.url?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("URL: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    logsLoading = true
                    logsError = null
                    runCatching { kokapi.logs.getForCache(cache.code, 10) }
                        .onSuccess { logs = it }
                        .onFailure { logsError = it.message ?: "Error loading logs" }
                    logsLoading = false
                }
            }) { Text("Load logs") }

            Button(onClick = {
                scope.launch {
                    gpxLoading = true
                    gpxError = null
                    runCatching { kokapi.formatters.gpx(listOf(cache.code)) }
                        .onSuccess { gpx = it.take(500) }
                        .onFailure { gpxError = it.message ?: "GPX error" }
                    gpxLoading = false
                }
            }) { Text("GPX export") }

            if (kokapi.auth.isAuthenticated) {
                Button(onClick = {
                    scope.launch {
                        runCatching { kokapi.caches.mark(cache.code, watched = true) }
                            .onSuccess { markResult = "Marked as watched!" }
                            .onFailure { markResult = "Error: ${it.message}" }
                    }
                }) { Text("Mark watched") }
            }
        }

        markResult?.let {
            Text(it, color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }

        if (logsLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            logsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (logs.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Logs (${logs.size}):", style = MaterialTheme.typography.labelLarge)
                logs.forEach { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Text("${log.user.username} — ${log.type.name} on ${log.date}")
                            if (log.comment.isNotBlank()) {
                                Text(
                                    log.comment.take(200),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        if (gpxLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            gpxError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            gpx?.let {
                Spacer(Modifier.height(8.dp))
                Text("GPX (first 500 chars):", style = MaterialTheme.typography.labelLarge)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
