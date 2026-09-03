package dev.fleetworks.memo.chat

sealed interface ChatEvent {
    data class Message(val message: ChatMessage) : ChatEvent
    data class ToolActivity(val text: String) : ChatEvent
    data class Error(val text: String) : ChatEvent
}
