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
import kotlinx.coroutines.flow.toList
import pl.kokapi.KokapiClient
import pl.kokapi.endpoints.SearchFilters
import pl.kokapi.model.Geocache
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.LogCapabilities
import pl.kokapi.model.enum.LogType

// Top-level tab: Caches vs Logs
private enum class CachesTab { CACHES, LOGS }

// Search mode within Caches tab
private enum class SearchMode { NEAREST, BBOX, SEARCH_ALL, BY_CODE, BY_CODES, BY_URL, SEARCH_SAVE, SEARCH_AND_RETRIEVE, FLOW }

@Composable
fun CachesScreen(kokapi: KokapiClient) {
    var topTab by remember { mutableStateOf(CachesTab.CACHES) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Caches & Logs", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { topTab = CachesTab.CACHES },
                colors = if (topTab == CachesTab.CACHES)
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { Text("Caches") }
            TextButton(
                onClick = { topTab = CachesTab.LOGS },
                colors = if (topTab == CachesTab.LOGS)
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { Text("Logs") }
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        when (topTab) {
            CachesTab.CACHES -> CachesSubScreen(kokapi)
            CachesTab.LOGS -> LogsSubScreen(kokapi)
        }
    }
}

@Composable
private fun CachesSubScreen(kokapi: KokapiClient) {
    var searchMode by remember { mutableStateOf(SearchMode.NEAREST) }
    var caches by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCache by remember { mutableStateOf<Geocache?>(null) }
    val scope = rememberCoroutineScope()

    // Nearest params
    var lat by remember { mutableStateOf("50.0619") }
    var lon by remember { mutableStateOf("19.9368") }
    var radius by remember { mutableStateOf("10.0") }

    // BBox params
    var southLat by remember { mutableStateOf("50.0") }
    var westLon by remember { mutableStateOf("19.8") }
    var northLat by remember { mutableStateOf("50.1") }
    var eastLon by remember { mutableStateOf("20.0") }

    // SearchAll params
    var saName by remember { mutableStateOf("") }
    var saType by remember { mutableStateOf("") }
    var saDifficulty by remember { mutableStateOf("") }
    var saLimit by remember { mutableStateOf("20") }
    var saRecommendedOnly by remember { mutableStateOf(false) }
    var saFtfHunter by remember { mutableStateOf(false) }

    // ByCode params
    var byCodeInput by remember { mutableStateOf("") }
    var singleCache by remember { mutableStateOf<Geocache?>(null) }

    // ByUrl params
    var byUrlInput by remember { mutableStateOf("") }

    // ByCodes params
    var byCodesInput by remember { mutableStateOf("") }

    // SearchSave params
    var ssName by remember { mutableStateOf("") }
    var ssType by remember { mutableStateOf("") }
    var ssLimit by remember { mutableStateOf("20") }
    var ssSetId by remember { mutableStateOf<String?>(null) }

    // SearchAndRetrieve params
    var sarType by remember { mutableStateOf("") }
    var sarLimit by remember { mutableStateOf("20") }

    // Flow params
    var flowLat by remember { mutableStateOf("50.0619") }
    var flowLon by remember { mutableStateOf("19.9368") }
    var flowRadius by remember { mutableStateOf("5.0") }

    if (selectedCache != null) {
        CacheDetail(
            cache = selectedCache!!,
            kokapi = kokapi,
            onBack = { selectedCache = null }
        )
        return
    }

    if (singleCache != null && searchMode == SearchMode.BY_CODE) {
        CacheDetail(
            cache = singleCache!!,
            kokapi = kokapi,
            onBack = { singleCache = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Mode selector chips
        Text("Search mode:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SearchMode.entries.forEach { mode ->
                FilterChip(
                    selected = searchMode == mode,
                    onClick = {
                        searchMode = mode
                        caches = emptyList()
                        error = null
                        singleCache = null
                    },
                    label = {
                        Text(
                            when (mode) {
                                SearchMode.NEAREST -> "Nearest"
                                SearchMode.BBOX -> "BBox"
                                SearchMode.SEARCH_ALL -> "SearchAll"
                                SearchMode.BY_CODE -> "ByCode"
                                SearchMode.BY_CODES -> "ByCodes"
                                SearchMode.BY_URL -> "ByUrl"
                                SearchMode.SEARCH_SAVE -> "SearchSave"
                                SearchMode.SEARCH_AND_RETRIEVE -> "S&Retrieve"
                                SearchMode.FLOW -> "Flow"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (searchMode) {
            SearchMode.NEAREST -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lon, onValueChange = { lon = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = radius, onValueChange = { radius = it }, label = { Text("Radius km") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching { kokapi.caches.search(lat.toDouble(), lon.toDouble(), radius.toDouble()) }
                            .onSuccess { caches = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("Search") }
            }

            SearchMode.BBOX -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = southLat, onValueChange = { southLat = it }, label = { Text("S lat") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = westLon, onValueChange = { westLon = it }, label = { Text("W lon") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = northLat, onValueChange = { northLat = it }, label = { Text("N lat") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = eastLon, onValueChange = { eastLon = it }, label = { Text("E lon") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching { kokapi.caches.bbox(southLat.toDouble(), westLon.toDouble(), northLat.toDouble(), eastLon.toDouble()) }
                            .onSuccess { caches = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("Search") }
            }

            SearchMode.SEARCH_ALL -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = saName, onValueChange = { saName = it }, label = { Text("Name") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = saType, onValueChange = { saType = it }, label = { Text("Type") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = saDifficulty, onValueChange = { saDifficulty = it }, label = { Text("Difficulty (e.g. 2-3)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = saLimit, onValueChange = { saLimit = it }, label = { Text("Limit") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = saRecommendedOnly, onClick = { saRecommendedOnly = !saRecommendedOnly }, label = { Text("Recommended only") })
                    FilterChip(selected = saFtfHunter, onClick = { saFtfHunter = !saFtfHunter }, label = { Text("FTF hunter") })
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching {
                            kokapi.caches.searchAll(SearchFilters(
                                name = saName.takeIf { it.isNotBlank() },
                                type = saType.takeIf { it.isNotBlank() }?.let { listOf(it) },
                                difficulty = saDifficulty.takeIf { it.isNotBlank() },
                                limit = saLimit.toIntOrNull() ?: 20,
                                recommendedOnly = saRecommendedOnly,
                                ftfHunter = saFtfHunter,
                            ))
                        }
                            .onSuccess { caches = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("Search All") }
            }

            SearchMode.BY_CODE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = byCodeInput,
                        onValueChange = { byCodeInput = it },
                        label = { Text("Cache code (e.g. OP1234)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true; error = null; singleCache = null
                                runCatching { kokapi.caches.get(byCodeInput.trim()) }
                                    .onSuccess { singleCache = it }
                                    .onFailure { error = it.message ?: "Unknown error" }
                                loading = false
                            }
                        },
                        enabled = byCodeInput.isNotBlank()
                    ) { Text("Get") }
                }
            }

            SearchMode.BY_URL -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = byUrlInput,
                        onValueChange = { byUrlInput = it },
                        label = { Text("Opencaching URL") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true; error = null
                                runCatching { kokapi.caches.searchByUrls(listOf(byUrlInput.trim())) }
                                    .onSuccess { caches = it }
                                    .onFailure { error = it.message ?: "Unknown error" }
                                loading = false
                            }
                        },
                        enabled = byUrlInput.isNotBlank()
                    ) { Text("Search") }
                }
            }

            SearchMode.BY_CODES -> {
                OutlinedTextField(
                    value = byCodesInput,
                    onValueChange = { byCodesInput = it },
                    label = { Text("Cache codes (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val codes = byCodesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            if (codes.isEmpty()) return@launch
                            loading = true; error = null
                            runCatching { kokapi.caches.getMultiple(codes) }
                                .onSuccess { caches = it }
                                .onFailure { error = it.message ?: "Unknown error" }
                            loading = false
                        }
                    },
                    enabled = byCodesInput.isNotBlank()
                ) { Text("Get multiple") }
            }

            SearchMode.SEARCH_SAVE -> {
                Text("Saves a search set on the server and returns its ID.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = ssName, onValueChange = { ssName = it }, label = { Text("Name filter") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = ssType, onValueChange = { ssType = it }, label = { Text("Type") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = ssLimit, onValueChange = { ssLimit = it }, label = { Text("Limit") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null; ssSetId = null
                        runCatching {
                            kokapi.caches.searchSave(SearchFilters(
                                name = ssName.takeIf { it.isNotBlank() },
                                type = ssType.takeIf { it.isNotBlank() }?.let { listOf(it) },
                                limit = ssLimit.toIntOrNull() ?: 20,
                            ))
                        }
                            .onSuccess { ssSetId = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("SearchSave") }
                ssSetId?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Set ID: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            SearchMode.SEARCH_AND_RETRIEVE -> {
                Text("Single-call search + fetch (no round-trips).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sarType, onValueChange = { sarType = it }, label = { Text("Type") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = sarLimit, onValueChange = { sarLimit = it }, label = { Text("Limit") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching {
                            kokapi.caches.searchAndRetrieve(SearchFilters(
                                type = sarType.takeIf { it.isNotBlank() }?.let { listOf(it) },
                                limit = sarLimit.toIntOrNull() ?: 20,
                            ))
                        }
                            .onSuccess { caches = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("Search & Retrieve") }
            }

            SearchMode.FLOW -> {
                Text("Paginates nearest-caches via Kotlin Flow.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = flowLat, onValueChange = { flowLat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = flowLon, onValueChange = { flowLon = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = flowRadius, onValueChange = { flowRadius = it }, label = { Text("Radius km") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching {
                            kokapi.caches.searchFlow(
                                lat = flowLat.toDouble(),
                                lon = flowLon.toDouble(),
                                radiusKm = flowRadius.toDouble(),
                                pageSize = 20
                            ).toList()
                        }
                            .onSuccess { caches = it }
                            .onFailure { error = it.message ?: "Unknown error" }
                        loading = false
                    }
                }) { Text("Collect flow") }
            }
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Spacer(Modifier.height(8.dp))
            if (caches.isNotEmpty()) {
                Text("Results: ${caches.size}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                caches.forEach { cache ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { selectedCache = cache }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(cache.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${cache.code} · ${cache.type.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("D${cache.difficulty} / T${cache.terrain}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsSubScreen(kokapi: KokapiClient) {
    var logUuid by remember { mutableStateOf("") }
    var singleLog by remember { mutableStateOf<GeocacheLog?>(null) }
    var singleLogLoading by remember { mutableStateOf(false) }
    var singleLogError by remember { mutableStateOf<String?>(null) }

    var entriesInput by remember { mutableStateOf("") }
    var entriesResult by remember { mutableStateOf<Map<String, GeocacheLog>>(emptyMap()) }
    var entriesLoading by remember { mutableStateOf(false) }
    var entriesError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Get log by UUID", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = logUuid,
                onValueChange = { logUuid = it },
                label = { Text("Log UUID") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        singleLogLoading = true; singleLogError = null; singleLog = null
                        runCatching { kokapi.logs.get(logUuid.trim()) }
                            .onSuccess { singleLog = it }
                            .onFailure { singleLogError = it.message ?: "Error" }
                        singleLogLoading = false
                    }
                },
                enabled = logUuid.isNotBlank()
            ) { Text("Get log") }
        }

        if (singleLogLoading) CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        singleLogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        singleLog?.let { log ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(10.dp)) {
                    Text("UUID: ${log.uuid}", style = MaterialTheme.typography.labelSmall)
                    Text("Cache: ${log.cacheCode} — ${log.type.name} on ${log.date}")
                    Text("By: ${log.user.username}")
                    if (log.comment.isNotBlank()) Text(log.comment.take(300), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Get multiple log entries", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = entriesInput,
            onValueChange = { entriesInput = it },
            label = { Text("Comma-separated UUIDs") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                scope.launch {
                    val uuids = entriesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (uuids.isEmpty()) return@launch
                    entriesLoading = true; entriesError = null
                    runCatching { kokapi.logs.getEntries(uuids) }
                        .onSuccess { entriesResult = it }
                        .onFailure { entriesError = it.message ?: "Error" }
                    entriesLoading = false
                }
            },
            enabled = entriesInput.isNotBlank()
        ) { Text("Get entries") }

        if (entriesLoading) CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        entriesError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (entriesResult.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("${entriesResult.size} entries:", style = MaterialTheme.typography.labelMedium)
            entriesResult.forEach { (uuid, log) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(uuid, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${log.cacheCode} — ${log.type.name} on ${log.date}")
                        Text("By: ${log.user.username}")
                        if (log.comment.isNotBlank()) Text(log.comment.take(200), style = MaterialTheme.typography.bodySmall)
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

    // Log capabilities
    var capabilities by remember { mutableStateOf<LogCapabilities?>(null) }
    var capsLoading by remember { mutableStateOf(true) }
    var capsError by remember { mutableStateOf<String?>(null) }

    // Submit log
    var selectedLogType by remember { mutableStateOf(LogType.FOUND) }
    var logComment by remember { mutableStateOf("") }
    var submitLoading by remember { mutableStateOf(false) }
    var submitResult by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submittedUuid by remember { mutableStateOf<String?>(null) }

    // Edit log
    var editComment by remember { mutableStateOf("") }
    var editLoading by remember { mutableStateOf(false) }
    var editResult by remember { mutableStateOf<String?>(null) }

    // Delete log
    var deleteLoading by remember { mutableStateOf(false) }
    var deleteResult by remember { mutableStateOf<String?>(null) }

    // Personal notes
    var personalNotes by remember { mutableStateOf("") }
    var notesLoading by remember { mutableStateOf(false) }
    var notesResult by remember { mutableStateOf<String?>(null) }

    // Cache edit (password-protected)
    var editPasswd by remember { mutableStateOf("") }
    var editCacheLoading by remember { mutableStateOf(false) }
    var editCacheResult by remember { mutableStateOf<String?>(null) }

    // Log image management (operate on submittedUuid's image)
    var imageCaption by remember { mutableStateOf("") }
    var imageLoading by remember { mutableStateOf(false) }
    var imageResult by remember { mutableStateOf<String?>(null) }
    var addedImageUuid by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(cache.code) {
        capsLoading = true; capsError = null
        runCatching { kokapi.logs.capabilities(cache.code) }
            .onSuccess { capabilities = it }
            .onFailure { capsError = it.message ?: "Error loading capabilities" }
        capsLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBack) { Text("Back to list") }

        // Cache info card
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
                cache.gcCode?.let { Text("GC code: $it") }
                cache.size2?.let { Text("Size: $it") }
                if (cache.recommendations > 0) Text("Recommendations: ${cache.recommendations}")
                if (cache.watchers > 0) Text("Watchers: ${cache.watchers}")
                if (cache.willattends > 0) Text("Will attend: ${cache.willattends}")
                cache.rating?.let { Text("Rating: ${"%.1f".format(it)}") }
                cache.ratingVotes?.let { Text("Rating votes: $it") }
                cache.myRating?.let { Text("My rating: $it/5") }
                cache.isFound?.let { Text("Found by me: $it") }
                cache.isNotFound?.let { Text("DNF by me: $it") }
                cache.isWatched?.let { Text("Watched: $it") }
                cache.isIgnored?.let { Text("Ignored: $it") }
                cache.needsMaintenance?.let { Text("Needs maintenance: $it") }
                cache.myNotes?.takeIf { it.isNotEmpty() }?.let { Text("My notes: $it") }
                if (cache.latestLogs.isNotEmpty()) Text("Latest logs: ${cache.latestLogs.size}")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Log capabilities section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Log Capabilities", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                if (capsLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    capsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    capabilities?.let { caps ->
                        Text("Can add: ${caps.canAdd}")
                        Text("Requires password: ${caps.requiresPassword}")
                        Text("Available types: ${caps.availableTypes.joinToString(", ")}")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Submit log section
        if (kokapi.auth.isAuthenticated && capabilities?.canAdd == true) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Submit Log", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))

                    val logTypes = listOf(
                        "Found It" to LogType.FOUND,
                        "Didn't find it" to LogType.NOT_FOUND,
                        "Comment" to LogType.COMMENT
                    )
                    Text("Log type:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        logTypes.forEach { (label, type) ->
                            FilterChip(
                                selected = selectedLogType == type,
                                onClick = { selectedLogType = type },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = logComment,
                        onValueChange = { logComment = it },
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                submitLoading = true; submitError = null; submitResult = null
                                runCatching { kokapi.logs.submit(cache.code, selectedLogType, logComment) }
                                    .onSuccess { uuid ->
                                        submittedUuid = uuid
                                        submitResult = "Log submitted! UUID: $uuid"
                                    }
                                    .onFailure { submitError = it.message ?: "Submit error" }
                                submitLoading = false
                            }
                        },
                        enabled = !submitLoading
                    ) { Text("Submit") }

                    if (submitLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    submitError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    submitResult?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

                    // Post-submit: image management
                    submittedUuid?.let { uuid ->
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text("Log Image Management", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = imageCaption,
                            onValueChange = { imageCaption = it },
                            label = { Text("Caption") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        imageLoading = true; imageResult = null
                                        // Use a tiny 1×1 red pixel PNG in base64 as test image
                                        val tiny1x1Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI6QAAAABJRU5ErkJggg=="
                                        runCatching { kokapi.logs.addImage(uuid, tiny1x1Png, caption = imageCaption.takeIf { it.isNotBlank() }) }
                                            .onSuccess { imgUuid ->
                                                addedImageUuid = imgUuid
                                                imageResult = "Image added! UUID: $imgUuid"
                                            }
                                            .onFailure { imageResult = "Error: ${it.message}" }
                                        imageLoading = false
                                    }
                                },
                                enabled = !imageLoading
                            ) { Text("Add image") }

                            addedImageUuid?.let { imgUuid ->
                                Button(
                                    onClick = {
                                        scope.launch {
                                            imageLoading = true; imageResult = null
                                            runCatching { kokapi.logs.editImage(imgUuid, caption = imageCaption.takeIf { it.isNotBlank() }) }
                                                .onSuccess { imageResult = "Image updated!" }
                                                .onFailure { imageResult = "Error: ${it.message}" }
                                            imageLoading = false
                                        }
                                    },
                                    enabled = !imageLoading
                                ) { Text("Edit image") }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            imageLoading = true; imageResult = null
                                            runCatching { kokapi.logs.deleteImage(imgUuid) }
                                                .onSuccess { addedImageUuid = null; imageResult = "Image deleted!" }
                                                .onFailure { imageResult = "Error: ${it.message}" }
                                            imageLoading = false
                                        }
                                    },
                                    enabled = !imageLoading
                                ) { Text("Delete image") }
                            }
                        }
                        if (imageLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        imageResult?.let { Text(it) }
                    }

                    // Post-submit actions
                    submittedUuid?.let { uuid ->
                        Spacer(Modifier.height(8.dp))
                        Text("Actions for submitted log:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        deleteLoading = true; deleteResult = null
                                        runCatching { kokapi.logs.delete(uuid) }
                                            .onSuccess { deleteResult = "Log deleted" }
                                            .onFailure { deleteResult = "Delete error: ${it.message}" }
                                        deleteLoading = false
                                    }
                                },
                                enabled = !deleteLoading
                            ) { Text("Delete this log") }
                        }
                        if (deleteLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        deleteResult?.let { Text(it) }

                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editComment,
                            onValueChange = { editComment = it },
                            label = { Text("New comment") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    editLoading = true; editResult = null
                                    runCatching { kokapi.logs.edit(uuid, comment = editComment) }
                                        .onSuccess { editResult = "Comment updated!" }
                                        .onFailure { editResult = "Edit error: ${it.message}" }
                                    editLoading = false
                                }
                            },
                            enabled = !editLoading && editComment.isNotBlank()
                        ) { Text("Edit comment") }
                        if (editLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        editResult?.let { Text(it) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Personal notes section
        if (kokapi.auth.isAuthenticated) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Personal Notes", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = personalNotes,
                        onValueChange = { personalNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                notesLoading = true; notesResult = null
                                runCatching { kokapi.caches.savePersonalNotes(cache.code, personalNotes) }
                                    .onSuccess { notesResult = "Notes saved!" }
                                    .onFailure { notesResult = "Error: ${it.message}" }
                                notesLoading = false
                            }
                        },
                        enabled = !notesLoading
                    ) { Text("Save notes") }
                    if (notesLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    notesResult?.let { Text(it) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Cache edit (for password-protected caches)
        if (kokapi.auth.isAuthenticated) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Cache Edit (password-protected)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Verifies access to a password-protected cache.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = editPasswd,
                            onValueChange = { editPasswd = it },
                            label = { Text("Password (if required)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    editCacheLoading = true; editCacheResult = null
                                    runCatching { kokapi.caches.edit(cache.code, editPasswd.takeIf { it.isNotBlank() }) }
                                        .onSuccess { editCacheResult = "Edit OK" }
                                        .onFailure { editCacheResult = "Error: ${it.message}" }
                                    editCacheLoading = false
                                }
                            },
                            enabled = !editCacheLoading
                        ) { Text("Edit") }
                    }
                    if (editCacheLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    editCacheResult?.let { Text(it) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Logs + GPX actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    logsLoading = true; logsError = null
                    runCatching { kokapi.logs.getForCache(cache.code, 10) }
                        .onSuccess { logs = it }
                        .onFailure { logsError = it.message ?: "Error loading logs" }
                    logsLoading = false
                }
            }) { Text("Load logs") }

            Button(onClick = {
                scope.launch {
                    gpxLoading = true; gpxError = null
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
                                Text(log.comment.take(200), style = MaterialTheme.typography.bodySmall)
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
