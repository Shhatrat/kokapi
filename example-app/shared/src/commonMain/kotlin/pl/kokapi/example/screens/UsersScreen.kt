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
    val scope = rememberCoroutineScope()

    LaunchedEffect(kokapi.auth.isAuthenticated) {
        if (kokapi.auth.isAuthenticated) {
            runCatching { kokapi.users.me() }
                .onSuccess { meUser = it }
                .onFailure { meError = it.message }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(u.username, style = MaterialTheme.typography.titleMedium)
                    Text("UUID: ${u.uuid}")
                    Text("Caches found: ${u.cachesFound}")
                    Text("Caches hidden: ${u.hiddenCount}")
                    Text("Rcmds given: ${u.rcmdsGiven}")
                    u.rcmdsLeft?.let { Text("Rcmds left: $it") }
                    u.profileUrl.takeIf { it.isNotBlank() }?.let { Text("Profile: $it", style = MaterialTheme.typography.bodySmall) }
                }
            }

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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    items(logs) { log ->
                        Card(modifier = Modifier.fillMaxWidth()) {
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
        }
    }
}
