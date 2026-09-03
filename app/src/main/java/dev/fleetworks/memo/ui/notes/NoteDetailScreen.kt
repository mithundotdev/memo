package dev.fleetworks.memo.ui.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.core.NoteRepository
import dev.fleetworks.memo.core.NoteStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(repo: NoteRepository, noteId: String, onBack: () -> Unit) {
    val vm: NoteDetailViewModel = viewModel(key = noteId, factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NoteDetailViewModel(repo, noteId) as T
    })
    val title by vm.title.collectAsState()
    val body by vm.body.collectAsState()
    val links by vm.backlinks.collectAsState()
    val saveState by vm.saveState.collectAsState()
    var preview by remember { mutableStateOf(false) }
    var bodyField by remember { mutableStateOf(TextFieldValue()) }
    LaunchedEffect(noteId) { vm.load(); vm.startAutoSave() }
    LaunchedEffect(body) { if (bodyField.text != body) bodyField = TextFieldValue(body, TextRange(body.length)) }

    fun apply(transform: (String, TextRange) -> Pair<String, TextRange>) {
        val (text, sel) = transform(bodyField.text, bodyField.selection)
        bodyField = TextFieldValue(text, sel)
        vm.body.value = text
    }

    fun wrap(before: String, after: String, placeholder: String) = apply { text, sel ->
        val inner = if (sel.collapsed) placeholder else text.substring(sel.start, sel.end)
        val replacement = before + inner + after
        val out = text.substring(0, sel.start) + replacement + text.substring(sel.end)
        val cursor = sel.start + before.length + inner.length + after.length
        out to TextRange(cursor)
    }

    fun prefixLines(prefix: String) = apply { text, sel ->
        val start = text.lastIndexOf('\n', sel.start - 1) + 1
        val out = text.substring(0, start) + prefix + text.substring(start)
        out to TextRange(sel.start + prefix.length, sel.end + prefix.length)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (saveState == SaveState.Saving) "Saving…" else "Saved") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(onClick = { preview = !preview }) { Text(if (preview) "Edit" else "Preview") }
                    IconButton(onClick = { vm.delete(onBack) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
            )
        },
        bottomBar = {
            if (!preview) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarkButton("B", { wrap("**", "**", "bold") })
                    MarkButton("I", { wrap("*", "*", "italic") })
                    MarkButton("H", { prefixLines("# ") })
                    MarkButton("List", { prefixLines("- ") })
                    MarkButton("Task", { prefixLines("- [ ] ") })
                    MarkButton("Link", { wrap("[", "](url)", "text") })
                    MarkButton("Code", { wrap("`", "`", "code") })
                }
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (preview) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title.ifBlank { "Untitled" }, style = MaterialTheme.typography.headlineMedium)
                    Text(body.ifBlank { "Nothing here yet." }, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                TextField(
                    value = title,
                    onValueChange = { vm.title.value = it },
                    placeholder = { Text("Title") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = bodyField,
                    onValueChange = { bodyField = it; vm.body.value = it.text },
                    placeholder = { Text("Write…") },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
            Text(
                "${NoteStats.wordCount("$title $body")} words",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (links.isNotEmpty()) {
                Text("Backlinks", style = MaterialTheme.typography.labelLarge)
                links.forEach { Text(it.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Composable
private fun MarkButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) { Text(label) }
}
