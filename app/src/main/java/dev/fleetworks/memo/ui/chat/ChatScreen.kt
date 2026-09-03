package dev.fleetworks.memo.ui.chat

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.AppContainer
import dev.fleetworks.memo.chat.ChatRole
import kotlinx.coroutines.delay

private val Suggestions = listOf("Summarize my notes", "Find my todos", "What did I note about work?")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(container: AppContainer) {
    val vm: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(container) as T
    })
    val messages by vm.messages.collectAsState()
    val activity by vm.activity.collectAsState()
    val input by vm.input.collectAsState()
    val busy by vm.busy.collectAsState()
    val err by vm.error.collectAsState()
    val pending by vm.pendingDelete.collectAsState()

    if (pending != null) {
        ModalBottomSheet(onDismissRequest = { vm.resolveDelete(false) }) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Delete this note?", style = MaterialTheme.typography.titleLarge)
                Text("Memo wants to delete ${pending?.title}.", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.resolveDelete(true) }) { Text("Delete") }
                    OutlinedButton(onClick = { vm.resolveDelete(false) }) { Text("Keep") }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (messages.isEmpty() && !busy) {
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text("Ask Memo anything", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                Text("It reads your notes first.", style = MaterialTheme.typography.bodyMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Suggestions.forEach { hint ->
                        SuggestionChip(onClick = { vm.input.value = hint; vm.send() }, label = { Text(hint) })
                    }
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(messages) { index, msg ->
                    val lastAssistant = index == messages.lastIndex && msg.role == ChatRole.Assistant
                    ElevatedCard(
                        Modifier.fillMaxWidth(),
                        shape = if (msg.role == ChatRole.User) MaterialTheme.shapes.large else MaterialTheme.shapes.medium
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (msg.role == ChatRole.User) "You" else "Memo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (lastAssistant) StreamingText(msg.text) else Text(msg.text)
                        }
                    }
                }
                itemsIndexed(activity) { _, line ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        AnimatedContent(line, label = "activity") { Text(it, style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }
        if (err != null) Text(err.orEmpty(), color = MaterialTheme.colorScheme.error)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextField(value = input, onValueChange = { vm.input.value = it }, modifier = Modifier.weight(1f), label = { Text("Ask") })
            Button(onClick = { vm.send() }, enabled = !busy) { Text("Send") }
        }
    }
}

@Composable
private fun StreamingText(text: String) {
    var shown by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        while (shown < text.length) {
            delay(12)
            shown++
        }
    }
    Text(if (shown >= text.length) text else text.take(shown))
}
