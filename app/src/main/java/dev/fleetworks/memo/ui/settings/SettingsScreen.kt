package dev.fleetworks.memo.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.core.profile.ProfileStore
import dev.fleetworks.memo.core.profile.ProviderPresets

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(profiles: ProfileStore) {
    val vm: SettingsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(profiles) as T
    })
    val baseUrl by vm.baseUrl.collectAsState()
    val apiKey by vm.apiKey.collectAsState()
    val model by vm.model.collectAsState()
    val status by vm.status.collectAsState()
    val busy by vm.busy.collectAsState()
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.load() }
    val selected = ProviderPresets.all.find { it.baseUrl == baseUrl }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Provider", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProviderPresets.all.forEach { preset ->
                FilterChip(
                    selected = selected == preset,
                    onClick = { vm.applyPreset(preset) },
                    label = { Text(preset.name) }
                )
            }
        }
        TextField(value = baseUrl, onValueChange = { vm.baseUrl.value = it }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
        TextField(
            value = apiKey,
            onValueChange = { vm.apiKey.value = it },
            label = { Text("API key") },
            visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { revealed = !revealed }) { Text(if (revealed) "Hide" else "Show") } },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = model,
            onValueChange = { vm.model.value = it },
            label = { Text("Model") },
            placeholder = { Text(selected?.modelHint.orEmpty()) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.save() }) { Text("Save") }
            OutlinedButton(onClick = { vm.test() }, enabled = !busy) { Text("Test connection") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dot = when {
                busy -> MaterialTheme.colorScheme.onSurfaceVariant
                status == "ok" || status == "saved" -> MaterialTheme.colorScheme.primary
                status == null -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.error
            }
            Box(Modifier.size(10.dp).background(dot, CircleShape))
            Text(if (busy) "testing…" else status.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
