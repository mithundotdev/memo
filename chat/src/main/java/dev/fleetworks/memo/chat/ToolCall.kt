package dev.fleetworks.memo.chat

data class ToolCall(
    val name: ToolName,
    val argsJson: String,
    val result: String = ""
)
