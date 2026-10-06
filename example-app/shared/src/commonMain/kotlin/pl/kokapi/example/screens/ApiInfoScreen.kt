package pl.kokapi.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.kokapi.KokapiClient
import pl.kokapi.model.ApiMethod
import pl.kokapi.model.ApiMethodSummary
import pl.kokapi.model.ApiStats
import pl.kokapi.model.Installation
import pl.kokapi.model.ReplicateInfo

@Composable
fun ApiInfoScreen(kokapi: KokapiClient) {
    var installation by remember { mutableStateOf<Installation?>(null) }
    var installationError by remember { mutableStateOf<String?>(null) }
    var allInstallations by remember { mutableStateOf<List<Installation>>(emptyList()) }
    var allInstallationsLoading by remember { mutableStateOf(false) }
    var allInstallationsError by remember { mutableStateOf<String?>(null) }

    var stats by remember { mutableStateOf<ApiStats?>(null) }
    var statsError by remember { mutableStateOf<String?>(null) }

    var replicateInfo by remember { mutableStateOf<ReplicateInfo?>(null) }
    var replicateError by remember { mutableStateOf<String?>(null) }
    var currentRevision by remember { mutableStateOf<Long?>(null) }
    var currentRevisionLoading by remember { mutableStateOf(false) }
    var currentRevisionError by remember { mutableStateOf<String?>(null) }
    var fullDumpUrl by remember { mutableStateOf<String?>(null) }
    var fullDumpLoading by remember { mutableStateOf(false) }
    var fullDumpError by remember { mutableStateOf<String?>(null) }

    var methods by remember { mutableStateOf<List<ApiMethodSummary>>(emptyList()) }
    var methodsLoading by remember { mutableStateOf(false) }
    var methodsError by remember { mutableStateOf<String?>(null) }
    var methodsLoaded by remember { mutableStateOf(false) }
    var expandedMethod by remember { mutableStateOf<String?>(null) }
    var methodDetails by remember { mutableStateOf<Map<String, ApiMethod>>(emptyMap()) }
    var methodDetailLoading by remember { mutableStateOf<String?>(null) }

    // Report issue
    var reportMethodPath by remember { mutableStateOf("") }
    var reportDescription by remember { mutableStateOf("") }
    var reportLoading by remember { mutableStateOf(false) }
    var reportResult by remember { mutableStateOf<String?>(null) }

    // Formatters
    var formatterCode by remember { mutableStateOf("OP9D98") }
    var formatterResult by remember { mutableStateOf<String?>(null) }
    var formatterLoading by remember { mutableStateOf(false) }
    var formatterError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching { kokapi.apiSrv.installation() }
            .onSuccess { installation = it }
            .onFailure { installationError = it.message }

        runCatching { kokapi.apiSrv.stats() }
            .onSuccess { stats = it }
            .onFailure { statsError = it.message }

        runCatching { kokapi.replicate.info() }
            .onSuccess { replicateInfo = it }
            .onFailure { replicateError = it.message }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("API Info", style = MaterialTheme.typography.headlineMedium)
        }

        // Installation card
        item {
            SectionCard(title = "Installation") {
                installationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                installation?.let { inst ->
                    InfoRow("Site name", inst.siteName)
                    InfoRow("Site URL", inst.siteUrl)
                    InfoRow("OKAPI base URL", inst.okapiBaseUrl)
                    InfoRow("OKAPI revision", inst.okapiRevision.toString())
                    inst.registrationUrl?.let { InfoRow("Registration URL", it) }
                } ?: run {
                    if (installationError == null) CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("All installations", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        scope.launch {
                            allInstallationsLoading = true; allInstallationsError = null
                            runCatching { kokapi.apiSrv.installations() }
                                .onSuccess { allInstallations = it }
                                .onFailure { allInstallationsError = it.message ?: "Error" }
                            allInstallationsLoading = false
                        }
                    },
                    enabled = !allInstallationsLoading
                ) { Text("List all installations") }
                if (allInstallationsLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                allInstallationsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (allInstallations.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("${allInstallations.size} installations:", style = MaterialTheme.typography.labelSmall)
                    allInstallations.forEach { inst ->
                        Text("• ${inst.siteName} — ${inst.siteUrl}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Stats card
        item {
            SectionCard(title = "Stats") {
                statsError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                stats?.let { s ->
                    InfoRow("Cache count", s.cacheCount.toString())
                } ?: run {
                    if (statsError == null) CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }
            }
        }

        // Replicate Info card
        item {
            SectionCard(title = "Replicate Info") {
                replicateError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                replicateInfo?.let { info ->
                    InfoRow("Revision", info.revision.toString())
                    info.fulldumpUrl?.let { InfoRow("Full dump URL", it) }
                } ?: run {
                    if (replicateError == null) CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                currentRevisionLoading = true; currentRevisionError = null
                                runCatching { kokapi.replicate.currentRevision() }
                                    .onSuccess { currentRevision = it }
                                    .onFailure { currentRevisionError = it.message ?: "Error" }
                                currentRevisionLoading = false
                            }
                        },
                        enabled = !currentRevisionLoading
                    ) { Text("Current revision") }

                    Button(
                        onClick = {
                            scope.launch {
                                fullDumpLoading = true; fullDumpError = null
                                runCatching { kokapi.replicate.fullDump() }
                                    .onSuccess { fullDumpUrl = it.take(200) }
                                    .onFailure { fullDumpError = it.message ?: "Error" }
                                fullDumpLoading = false
                            }
                        },
                        enabled = !fullDumpLoading
                    ) { Text("Full dump URL") }
                }

                if (currentRevisionLoading || fullDumpLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                currentRevisionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                currentRevision?.let { InfoRow("Current revision", it.toString()) }
                fullDumpError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                fullDumpUrl?.let {
                    Text("Full dump URL:", style = MaterialTheme.typography.labelSmall)
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Formatters card
        item {
            SectionCard(title = "Formatters") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatterCode,
                        onValueChange = { formatterCode = it },
                        label = { Text("Cache code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (formatterLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                }
                formatterError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                formatterResult?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                formatterLoading = true; formatterError = null; formatterResult = null
                                runCatching { kokapi.formatters.garmin(listOf(formatterCode.trim())) }
                                    .onSuccess { formatterResult = "Garmin: Downloaded ${it.size} bytes" }
                                    .onFailure { formatterError = it.message ?: "Error" }
                                formatterLoading = false
                            }
                        },
                        enabled = formatterCode.isNotBlank() && !formatterLoading
                    ) { Text("Garmin (.gpi)") }

                    Button(
                        onClick = {
                            scope.launch {
                                formatterLoading = true; formatterError = null; formatterResult = null
                                runCatching { kokapi.formatters.ggz(listOf(formatterCode.trim())) }
                                    .onSuccess { formatterResult = "GGZ: Downloaded ${it.size} bytes" }
                                    .onFailure { formatterError = it.message ?: "Error" }
                                formatterLoading = false
                            }
                        },
                        enabled = formatterCode.isNotBlank() && !formatterLoading
                    ) { Text("GGZ") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                formatterLoading = true; formatterError = null; formatterResult = null
                                runCatching { kokapi.formatters.mapTile(z = 13, x = 4565, y = 2848) }
                                    .onSuccess { formatterResult = "Map tile: Downloaded ${it.size} bytes" }
                                    .onFailure { formatterError = it.message ?: "Error" }
                                formatterLoading = false
                            }
                        },
                        enabled = !formatterLoading
                    ) { Text("Map tile (Kraków)") }

                    Button(
                        onClick = {
                            scope.launch {
                                formatterLoading = true; formatterError = null; formatterResult = null
                                runCatching { kokapi.formatters.gpx(listOf(formatterCode.trim())) }
                                    .onSuccess { formatterResult = "GPX (first 300 chars):\n${it.take(300)}" }
                                    .onFailure { formatterError = it.message ?: "Error" }
                                formatterLoading = false
                            }
                        },
                        enabled = formatterCode.isNotBlank() && !formatterLoading
                    ) { Text("GPX") }
                }
            }
        }

        // API Method Index card
        item {
            SectionCard(title = "API Method Index") {
                methodsError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (!methodsLoaded) {
                    Button(
                        onClick = {
                            scope.launch {
                                methodsLoading = true
                                methodsError = null
                                runCatching { kokapi.apiRef.methodIndex() }
                                    .onSuccess {
                                        methods = it
                                        methodsLoaded = true
                                    }
                                    .onFailure { methodsError = it.message ?: "Error loading methods" }
                                methodsLoading = false
                            }
                        },
                        enabled = !methodsLoading
                    ) { Text("Load API methods") }
                    if (methodsLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    }
                }
                if (methods.isNotEmpty()) {
                    Text("${methods.size} methods (click for details):", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    methods.forEach { method ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (expandedMethod == method.methodPath) {
                                            expandedMethod = null
                                        } else {
                                            expandedMethod = method.methodPath
                                            if (!methodDetails.containsKey(method.methodPath)) {
                                                scope.launch {
                                                    methodDetailLoading = method.methodPath
                                                    runCatching { kokapi.apiRef.method(method.methodPath) }
                                                        .onSuccess { detail ->
                                                            methodDetails = methodDetails + (method.methodPath to detail)
                                                        }
                                                        .onFailure { /* ignore */ }
                                                    methodDetailLoading = null
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    method.methodPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    if (expandedMethod == method.methodPath) "▲" else "▼",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (method.description.isNotBlank()) {
                                Text(
                                    method.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                                )
                            }
                            if (expandedMethod == method.methodPath) {
                                if (methodDetailLoading == method.methodPath) {
                                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                                }
                                methodDetails[method.methodPath]?.let { detail ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 4.dp)
                                    ) {
                                        Column(Modifier.padding(8.dp)) {
                                            if (detail.description.isNotBlank()) {
                                                Text("Description:", style = MaterialTheme.typography.labelSmall)
                                                Text(detail.description, style = MaterialTheme.typography.bodySmall)
                                            }
                                            if (detail.availableFields.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text("Available fields:", style = MaterialTheme.typography.labelSmall)
                                                Text(detail.availableFields.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Report issue", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = reportMethodPath,
                    onValueChange = { reportMethodPath = it },
                    label = { Text("Method path") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = reportDescription,
                    onValueChange = { reportDescription = it },
                    label = { Text("Issue description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        scope.launch {
                            reportLoading = true; reportResult = null
                            runCatching { kokapi.apiRef.reportIssue(reportMethodPath.trim(), reportDescription.trim()) }
                                .onSuccess { ok -> reportResult = if (ok) "Issue reported successfully!" else "Server rejected the report." }
                                .onFailure { reportResult = "Error: ${it.message}" }
                            reportLoading = false
                        }
                    },
                    enabled = reportMethodPath.isNotBlank() && reportDescription.isNotBlank() && !reportLoading
                ) { Text("Report") }
                if (reportLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                reportResult?.let { Text(it) }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}
