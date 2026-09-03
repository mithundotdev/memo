package dev.fleetworks.memo.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.core.profile.ProfileStore

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
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings")
        TextField(value = baseUrl, onValueChange = { vm.baseUrl.value = it }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
        TextField(value = apiKey, onValueChange = { vm.apiKey.value = it }, label = { Text("API key") }, modifier = Modifier.fillMaxWidth())
        TextField(value = model, onValueChange = { vm.model.value = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.save() }) { Text("Save") }
            OutlinedButton(onClick = { vm.test() }, enabled = !busy) { Text("Test connection") }
        }
        if (status != null) Text(status.orEmpty())
    }
}
