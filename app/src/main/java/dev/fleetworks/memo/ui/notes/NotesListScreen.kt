package dev.fleetworks.memo.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.core.NoteRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesListScreen(repo: NoteRepository, onOpen: (String) -> Unit, onNew: () -> Unit) {
    val vm: NotesViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NotesViewModel(repo) as T
    })
    val notes by vm.notes.collectAsState()
    val tags by vm.allTags.collectAsState()
    val query by vm.query.collectAsState()
    val active by vm.activeTag.collectAsState()
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onNew) { Icon(Icons.Filled.Add, contentDescription = "New") }
    }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = query, onValueChange = { vm.query.value = it }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = active == tag,
                        onClick = { vm.activeTag.value = if (active == tag) null else tag },
                        label = { Text("#$tag") }
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(notes, key = { it.id }) { note ->
                    ListItem(
                        headlineContent = { Text(note.title.ifBlank { "Untitled" }) },
                        supportingContent = { Text(note.body.take(120)) },
                        modifier = Modifier.clickable { onOpen(note.id) }
                    )
                }
            }
        }
    }
}
