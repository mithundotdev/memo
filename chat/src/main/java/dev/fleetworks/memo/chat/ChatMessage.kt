package dev.fleetworks.memo.chat

import java.util.UUID

sealed interface ChatRole {
    data object User : ChatRole
    data object Assistant : ChatRole
    data object Tool : ChatRole
}

data class ChatMessage(
    val role: ChatRole,
    val text: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val turnId: String = UUID.randomUUID().toString()
)
