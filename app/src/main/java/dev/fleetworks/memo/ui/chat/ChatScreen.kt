package dev.fleetworks.memo.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fleetworks.memo.AppContainer
import dev.fleetworks.memo.chat.ChatRole

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
        AlertDialog(
            onDismissRequest = { vm.resolveDelete(false) },
            title = { Text("Delete note") },
            text = { Text("Delete ${pending?.title}?") },
            confirmButton = { TextButton(onClick = { vm.resolveDelete(true) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { vm.resolveDelete(false) }) { Text("Cancel") } }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(messages) { msg ->
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        (if (msg.role == ChatRole.User) "You: " else "Memo: ") + msg.text,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            items(activity) { line ->
                Text(line)
            }
        }
        if (err != null) Text(err.orEmpty())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextField(value = input, onValueChange = { vm.input.value = it }, modifier = Modifier.weight(1f), label = { Text("Ask") })
            Button(onClick = { vm.send() }, enabled = !busy) { Text("Send") }
        }
    }
}
