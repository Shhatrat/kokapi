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
import pl.kokapi.model.Attribute

@Composable
fun AttrsScreen(kokapi: KokapiClient) {
    var attrs by remember { mutableStateOf<List<Attribute>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Single attribute lookup
    var acodeInput by remember { mutableStateOf("") }
    var singleAttr by remember { mutableStateOf<Attribute?>(null) }
    var singleLoading by remember { mutableStateOf(false) }
    var singleError by remember { mutableStateOf<String?>(null) }

    // Multiple attributes lookup
    var acodesInput by remember { mutableStateOf("") }
    var multipleAttrs by remember { mutableStateOf<List<Attribute>>(emptyList()) }
    var multipleLoading by remember { mutableStateOf(false) }
    var multipleError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching { kokapi.attrs.attributeIndex() }
            .onSuccess { attrs = it }
            .onFailure { error = it.message ?: "Unknown error" }
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Attributes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Lookup specific attribute
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Lookup specific attribute", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = acodeInput,
                        onValueChange = { acodeInput = it },
                        label = { Text("Acode (e.g. A1)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                singleLoading = true; singleError = null; singleAttr = null
                                runCatching { kokapi.attrs.attribute(acodeInput.trim()) }
                                    .onSuccess { singleAttr = it }
                                    .onFailure { singleError = it.message ?: "Error" }
                                singleLoading = false
                            }
                        },
                        enabled = acodeInput.isNotBlank()
                    ) { Text("Get attribute") }
                }
                if (singleLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                singleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                singleAttr?.let { AttrRow(it) }

                Spacer(Modifier.height(8.dp))

                Text("Get multiple attributes", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = acodesInput,
                        onValueChange = { acodesInput = it },
                        label = { Text("Comma-separated acodes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val codes = acodesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                if (codes.isEmpty()) return@launch
                                multipleLoading = true; multipleError = null; multipleAttrs = emptyList()
                                runCatching { kokapi.attrs.attributes(codes) }
                                    .onSuccess { multipleAttrs = it }
                                    .onFailure { multipleError = it.message ?: "Error" }
                                multipleLoading = false
                            }
                        },
                        enabled = acodesInput.isNotBlank()
                    ) { Text("Get multiple") }
                }
                if (multipleLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                multipleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                multipleAttrs.forEach { attr -> AttrRow(attr) }
            }
        }

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text("${attrs.size} attributes (index)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(attrs) { attr ->
                    AttrRow(attr)
                }
            }
        }
    }
}

@Composable
private fun AttrRow(attr: Attribute) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(attr.name, style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        color = if (attr.isYes) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (attr.isYes) "Yes" else "No",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (attr.isYes) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Text(
                    attr.acode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                attr.description?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                attr.iconUrl?.let {
                    Spacer(Modifier.height(2.dp))
                    Text("Icon: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
