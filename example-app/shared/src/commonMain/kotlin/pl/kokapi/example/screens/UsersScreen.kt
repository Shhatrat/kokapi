package pl.kokapi.example.screens

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
import pl.kokapi.model.GeocacheLog
import pl.kokapi.model.User

@Composable
fun UsersScreen(kokapi: KokapiClient) {
    var username by remember { mutableStateOf("") }
    var user by remember { mutableStateOf<User?>(null) }
    var userLoading by remember { mutableStateOf(false) }
    var userError by remember { mutableStateOf<String?>(null) }
    var meUser by remember { mutableStateOf<User?>(null) }
    var meError by remember { mutableStateOf<String?>(null) }
    var logs by remember { mutableStateOf<List<GeocacheLog>>(emptyList()) }
    var logsLoading by remember { mutableStateOf(false) }
    var logsError by remember { mutableStateOf<String?>(null) }

    // Other lookup methods
    var uuidInput by remember { mutableStateOf("") }
    var uuidResult by remember { mutableStateOf<User?>(null) }
    var uuidLoading by remember { mutableStateOf(false) }
    var uuidError by remember { mutableStateOf<String?>(null) }

    var usernamesInput by remember { mutableStateOf("") }
    var usernamesResult by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var usernamesLoading by remember { mutableStateOf(false) }
    var usernamesError by remember { mutableStateOf<String?>(null) }

    var internalIdInput by remember { mutableStateOf("") }
    var internalIdResult by remember { mutableStateOf<User?>(null) }
    var internalIdLoading by remember { mutableStateOf(false) }
    var internalIdError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(kokapi.auth.isAuthenticated) {
        if (kokapi.auth.isAuthenticated) {
            runCatching { kokapi.users.me() }
                .onSuccess { meUser = it }
                .onFailure { meError = it.message }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Users", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        if (kokapi.auth.isAuthenticated) {
            Text("Logged-in user:", style = MaterialTheme.typography.labelLarge)
            meError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            meUser?.let { me ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(me.username, style = MaterialTheme.typography.titleMedium)
                        Text("UUID: ${me.uuid}")
                        Text("Caches found: ${me.cachesFound}")
                        Text("Caches hidden: ${me.hiddenCount}")
                        Text("Rcmds given: ${me.rcmdsGiven}")
                        me.rcmdsLeft?.let { Text("Rcmds left: $it") }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Look up user by username:", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        userLoading = true
                        userError = null
                        user = null
                        logs = emptyList()
                        logsError = null
                        runCatching { kokapi.users.byUsername(username.trim()) }
                            .onSuccess { user = it }
                            .onFailure { userError = it.message ?: "Error looking up user" }
                        userLoading = false
                    }
                },
                enabled = username.isNotBlank()
            ) { Text("Look up") }
        }

        Spacer(Modifier.height(8.dp))
        userError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (userLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        user?.let { u ->
            UserCard(u)
            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                scope.launch {
                    logsLoading = true
                    logsError = null
                    logs = emptyList()
                    runCatching { kokapi.logs.getUserLogs(u.uuid, 10) }
                        .onSuccess { logs = it }
                        .onFailure { logsError = it.message ?: "Error loading logs" }
                    logsLoading = false
                }
            }) { Text("Load their logs") }

            logsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (logsLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            } else if (logs.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Logs (${logs.size}):", style = MaterialTheme.typography.labelLarge)
                logs.forEach { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Text("${log.cacheCode} — ${log.type.name} on ${log.date}")
                            if (log.comment.isNotBlank()) {
                                Text(
                                    log.comment.take(150),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text("Other lookup methods", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        // By UUID
        Text("By UUID:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uuidInput,
                onValueChange = { uuidInput = it },
                label = { Text("User UUID") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        uuidLoading = true; uuidError = null; uuidResult = null
                        runCatching { kokapi.users.byUuid(uuidInput.trim()) }
                            .onSuccess { uuidResult = it }
                            .onFailure { uuidError = it.message ?: "Error" }
                        uuidLoading = false
                    }
                },
                enabled = uuidInput.isNotBlank()
            ) { Text("By UUID") }
        }
        if (uuidLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
        uuidError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        uuidResult?.let { UserCard(it) }

        Spacer(Modifier.height(12.dp))

        // By usernames (bulk)
        Text("By usernames (comma-separated):", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = usernamesInput,
                onValueChange = { usernamesInput = it },
                label = { Text("Usernames") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        val names = usernamesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (names.isEmpty()) return@launch
                        usernamesLoading = true; usernamesError = null; usernamesResult = emptyMap()
                        runCatching { kokapi.users.byUsernames(names) }
                            .onSuccess { usernamesResult = it }
                            .onFailure { usernamesError = it.message ?: "Error" }
                        usernamesLoading = false
                    }
                },
                enabled = usernamesInput.isNotBlank()
            ) { Text("By usernames") }
        }
        if (usernamesLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
        usernamesError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (usernamesResult.isNotEmpty()) {
            Text("${usernamesResult.size} results:", style = MaterialTheme.typography.labelSmall)
            usernamesResult.values.forEach { u -> UserCard(u) }
        }

        Spacer(Modifier.height(12.dp))

        // By internal ID
        Text("By internal ID:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = internalIdInput,
                onValueChange = { internalIdInput = it },
                label = { Text("Internal ID (number)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    scope.launch {
                        internalIdLoading = true; internalIdError = null; internalIdResult = null
                        runCatching { kokapi.users.byInternalId(internalIdInput.trim()) }
                            .onSuccess { internalIdResult = it }
                            .onFailure { internalIdError = it.message ?: "Error" }
                        internalIdLoading = false
                    }
                },
                enabled = internalIdInput.isNotBlank()
            ) { Text("By internal ID") }
        }
        if (internalIdLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
        internalIdError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        internalIdResult?.let { UserCard(it) }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun UserCard(u: User) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(u.username, style = MaterialTheme.typography.titleSmall)
            Text("UUID: ${u.uuid}", style = MaterialTheme.typography.bodySmall)
            Text("Caches found: ${u.cachesFound}", style = MaterialTheme.typography.bodySmall)
            Text("Caches hidden: ${u.hiddenCount}", style = MaterialTheme.typography.bodySmall)
            Text("Rcmds given: ${u.rcmdsGiven}", style = MaterialTheme.typography.bodySmall)
            u.rcmdsLeft?.let { Text("Rcmds left: $it", style = MaterialTheme.typography.bodySmall) }
            u.profileUrl.takeIf { it.isNotBlank() }?.let { Text("Profile: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
