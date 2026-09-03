package dev.fleetworks.memo.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.AppContainer
import dev.fleetworks.memo.chat.ChatEvent
import dev.fleetworks.memo.chat.ChatMessage
import dev.fleetworks.memo.chat.ChatRole
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PendingDelete(val title: String, val gate: CompletableDeferred<Boolean>)

class ChatViewModel(private val container: AppContainer) : ViewModel() {
    val input = MutableStateFlow("")
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _activity = MutableStateFlow<List<String>>(emptyList())
    val activity: StateFlow<List<String>> = _activity

    val pendingDelete = MutableStateFlow<PendingDelete?>(null)

    fun send() {
        val text = input.value.trim()
        if (text.isBlank() || busy.value) return
        input.value = ""
        _messages.value = _messages.value + ChatMessage(ChatRole.User, text)
        busy.value = true
        error.value = null
        viewModelScope.launch {
            try {
                val profile = container.profiles.observeProfile().first()
                if (profile == null || profile.apiKey.isBlank()) {
                    error.value = "set provider in Settings"
                    return@launch
                }
                container.chatRunner.runTurn(_messages.value, profile) { title ->
                    val gate = CompletableDeferred<Boolean>()
                    pendingDelete.value = PendingDelete(title, gate)
                    val ok = gate.await()
                    pendingDelete.value = null
                    ok
                }.collect { event ->
                    when (event) {
                        is ChatEvent.Message -> _messages.value = _messages.value + event.message
                        is ChatEvent.ToolActivity -> _activity.value = _activity.value + event.text
                        is ChatEvent.Error -> error.value = event.text
                    }
                }
            } finally {
                busy.value = false
            }
        }
    }

    fun resolveDelete(ok: Boolean) {
        pendingDelete.value?.gate?.complete(ok)
    }
}
