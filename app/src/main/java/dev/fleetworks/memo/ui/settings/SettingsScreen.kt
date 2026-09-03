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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
    val selectedName by vm.selectedPresetName.collectAsState()
    val saved by vm.saved.collectAsState()
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.load() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Bring your own key. Pick a provider, drop the key, test, save.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Section("Provider") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderPresets.all.forEach { preset ->
                    FilterChip(
                        selected = selectedName == preset.name,
                        onClick = { vm.applyPreset(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }
        }

        Section("Endpoint") {
            TextField(
                value = baseUrl,
                onValueChange = { vm.onBaseUrlChange(it) },
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = model,
                onValueChange = { vm.onModelChange(it) },
                label = { Text("Model") },
                singleLine = true,
                placeholder = { Text(ProviderPresets.byName(selectedName.orEmpty())?.modelHint.orEmpty()) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Section("Key") {
            TextField(
                value = apiKey,
                onValueChange = { vm.onKeyChange(it) },
                label = { Text("API key") },
                singleLine = true,
                visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { revealed = !revealed }) {
                        Text(if (revealed) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.save() }) { Text(if (saved) "Saved" else "Save") }
            OutlinedButton(onClick = { vm.test() }, enabled = !busy) {
                Text(if (busy) "Testing…" else "Test connection")
            }
        }

        StatusLine(busy = busy, status = status)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun StatusLine(busy: Boolean, status: String?) {
    val color = when {
        busy -> MaterialTheme.colorScheme.onSurfaceVariant
        status == "ok" || status == "saved" -> MaterialTheme.colorScheme.primary
        status == null -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.error
    }
    val label = when {
        busy -> "Testing…"
        status == "ok" -> "Connected. Send a message from Chat."
        status == "saved" -> "Saved."
        status == null -> "Pick a provider, paste a key, hit Test."
        else -> "Failed: $status"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
