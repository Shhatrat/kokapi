package pl.kokapi.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.kokapi.KokapiClient
import pl.kokapi.model.User

@Composable
fun AuthScreen(kokapi: KokapiClient) {
    var isAuthenticated by remember { mutableStateOf(kokapi.auth.isAuthenticated) }
    var authorizeUrl by remember { mutableStateOf<String?>(null) }
    var callbackInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginSuccess by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var meUser by remember { mutableStateOf<User?>(null) }
    var meError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            meUser = null
            meError = null
            runCatching { kokapi.users.me() }
                .onSuccess { meUser = it }
                .onFailure { meError = it.message }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Authentication", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (!isAuthenticated) {
            Text("Not logged in.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        loginLoading = true
                        loginError = null
                        runCatching { kokapi.auth.startLogin() }
                            .onSuccess { step -> authorizeUrl = step.authorizeUrl }
                            .onFailure { loginError = it.message ?: "Error starting login" }
                        loginLoading = false
                    }
                },
                enabled = !loginLoading
            ) { Text("Start OAuth Login") }

            if (loginLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }

            loginError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            authorizeUrl?.let { url ->
                Spacer(Modifier.height(16.dp))
                Text("Open this URL in your browser:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            url,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("After authorizing, enter the callback URL or PIN:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = callbackInput,
                    onValueChange = { callbackInput = it },
                    label = { Text("Callback URL or PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            loginLoading = true
                            loginError = null
                            loginSuccess = null
                            runCatching { kokapi.auth.completeLogin(callbackInput.trim()) }
                                .onSuccess { ok ->
                                    if (ok) {
                                        loginSuccess = "Login successful!"
                                        isAuthenticated = true
                                    } else {
                                        loginError = "Login failed — invalid verifier?"
                                    }
                                }
                                .onFailure { loginError = it.message ?: "Error completing login" }
                            loginLoading = false
                        }
                    },
                    enabled = callbackInput.isNotBlank() && !loginLoading
                ) { Text("Complete login") }

                loginSuccess?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Text("Logged in", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            meError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            meUser?.let { me ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(me.username, style = MaterialTheme.typography.titleMedium)
                        Text("Caches found: ${me.cachesFound}")
                        me.rcmdsLeft?.let { Text("Recommendations left: $it") }
                    }
                }
            } ?: run {
                if (meError == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    kokapi.auth.logout()
                    isAuthenticated = false
                    meUser = null
                    meError = null
                    authorizeUrl = null
                    callbackInput = ""
                    loginError = null
                    loginSuccess = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Logout") }
        }
    }
}
