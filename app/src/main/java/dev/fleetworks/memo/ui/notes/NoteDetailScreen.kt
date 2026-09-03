package dev.fleetworks.memo.ui.notes

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
import dev.fleetworks.memo.core.NoteRepository

@Composable
fun NoteDetailScreen(repo: NoteRepository, noteId: String, onBack: () -> Unit) {
    val vm: NoteDetailViewModel = viewModel(key = noteId, factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NoteDetailViewModel(repo, noteId) as T
    })
    val title by vm.title.collectAsState()
    val body by vm.body.collectAsState()
    val links by vm.backlinks.collectAsState()
    LaunchedEffect(noteId) { vm.load() }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextField(value = title, onValueChange = { vm.title.value = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        TextField(value = body, onValueChange = { vm.body.value = it }, label = { Text("Body") }, modifier = Modifier.fillMaxWidth().weight(1f))
        if (links.isNotEmpty()) {
            Text("Backlinks")
            links.forEach { Text(it.title.ifBlank { "Untitled" }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.save { onBack() } }) { Text("Save") }
            if (noteId != "new") {
                OutlinedButton(onClick = { vm.delete(onBack) }) { Text("Delete") }
            }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    }
}
