package pl.kokapi.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.kokapi.KokapiClient
import pl.kokapi.KokapiSite
import pl.kokapi.auth.EncryptedPrefsTokenStorage
import pl.kokapi.error.KokapiException
import pl.kokapi.model.Geocache

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kokapi = KokapiClient(
            site = KokapiSite.OPENCACHING_PL,
            consumerKey = BuildConfig.CONSUMER_KEY,
            consumerSecret = BuildConfig.CONSUMER_SECRET,
            callbackUrl = "kokapi://oauth",
            tokenStorage = EncryptedPrefsTokenStorage(this),
        )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CacheListScreen(kokapi)
                }
            }
        }
    }
}

class CacheViewModel(private val kokapi: KokapiClient) : ViewModel() {
    private val _caches = MutableStateFlow<List<Geocache>>(emptyList())
    val caches: StateFlow<List<Geocache>> = _caches

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun search(lat: Double, lon: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { kokapi.caches.search(lat, lon, radiusKm = 10.0) }
                .onSuccess { _caches.value = it }
                .onFailure { e ->
                    _error.value = when (e) {
                        is KokapiException.NetworkError -> "Network unavailable"
                        is KokapiException.ApiError -> "API error: ${e.errorCode}"
                        else -> e.message ?: "Unknown error"
                    }
                }
            _loading.value = false
        }
    }
}

@Composable
fun CacheListScreen(kokapi: KokapiClient) {
    val viewModel = remember { CacheViewModel(kokapi) }
    val caches by viewModel.caches.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.search(lat = 50.0619, lon = 19.9368)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("KoKAPI Example", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text(
                "Caches near Krakow: ${caches.size}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(caches) { cache ->
                    CacheCard(cache)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "D${cache.difficulty} / T${cache.terrain} · ${cache.founds} finds",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
