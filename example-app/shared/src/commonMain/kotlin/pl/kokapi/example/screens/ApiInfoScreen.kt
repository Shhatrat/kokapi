package pl.kokapi.example.screens

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
import pl.kokapi.model.ApiMethodSummary
import pl.kokapi.model.ApiStats
import pl.kokapi.model.Installation
import pl.kokapi.model.ReplicateInfo

@Composable
fun ApiInfoScreen(kokapi: KokapiClient) {
    var installation by remember { mutableStateOf<Installation?>(null) }
    var installationError by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf<ApiStats?>(null) }
    var statsError by remember { mutableStateOf<String?>(null) }
    var replicateInfo by remember { mutableStateOf<ReplicateInfo?>(null) }
    var replicateError by remember { mutableStateOf<String?>(null) }
    var methods by remember { mutableStateOf<List<ApiMethodSummary>>(emptyList()) }
    var methodsLoading by remember { mutableStateOf(false) }
    var methodsError by remember { mutableStateOf<String?>(null) }
    var methodsLoaded by remember { mutableStateOf(false) }
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
            }
        }

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
            }
        }

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
                    Text("${methods.size} methods:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    methods.forEach { method ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                method.methodPath,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (method.description.isNotBlank()) {
                            Text(
                                method.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }
                        HorizontalDivider()
                    }
                }
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
