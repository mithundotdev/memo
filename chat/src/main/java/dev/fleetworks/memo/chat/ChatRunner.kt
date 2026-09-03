package dev.fleetworks.memo.chat

import com.aallam.openai.api.chat.ChatMessage as RemoteMessage
import com.aallam.openai.api.chat.ChatRole as RemoteRole
import com.aallam.openai.api.chat.ToolCall as RemoteToolCall
import dev.fleetworks.memo.core.NoteRepository
import dev.fleetworks.memo.core.profile.ProviderProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatRunner(
    private val repo: NoteRepository,
    private val sourceFactory: (ProviderProfile) -> OpenAiChatSource = ::OpenAiChatSource
) {
    fun runTurn(
        history: List<ChatMessage>,
        profile: ProviderProfile,
        confirmDelete: suspend (title: String) -> Boolean
    ): Flow<ChatEvent> = flow {
        if (profile.apiKey.isBlank()) {
            emit(ChatEvent.Error("missing api key"))
            return@flow
        }
        val registry = ToolRegistry.create(repo)
        val source = sourceFactory(profile)
        val remote = mutableListOf(source.systemMessage())
        history.forEach { msg ->
            when (msg.role) {
                ChatRole.User -> remote.add(RemoteMessage(RemoteRole.User, content = msg.text))
                ChatRole.Assistant -> remote.add(RemoteMessage(RemoteRole.Assistant, content = msg.text))
                ChatRole.Tool -> Unit
            }
        }
        val seenIds = mutableSetOf<String>()
        repeat(MAX_TURNS) {
            val assistant = try {
                source.complete(remote.toList())
            } catch (e: Exception) {
                emit(ChatEvent.Error(e.message ?: "request failed"))
                return@flow
            }
            val calls = assistant.toolCalls.orEmpty().filterIsInstance<RemoteToolCall.Function>()
            if (calls.isEmpty()) {
                emit(ChatEvent.Message(ChatMessage(ChatRole.Assistant, assistant.content.orEmpty())))
                return@flow
            }
            remote.add(assistant)
            for (call in calls) {
                val wire = call.function.name
                val toolName = ToolName.fromWire(wire)
                if (toolName == null) {
                    remote.add(RemoteMessage(RemoteRole.Tool, toolCallId = call.id, name = wire, content = "unknown tool"))
                    continue
                }
                val args = call.function.arguments
                if (toolName == ToolName.DELETE_NOTE) {
                    val id = parseId(args)
                    if (id !in seenIds) {
                        val msg = "delete blocked: search first"
                        emit(ChatEvent.ToolActivity(msg))
                        remote.add(RemoteMessage(RemoteRole.Tool, toolCallId = call.id, name = wire, content = msg))
                        continue
                    }
                    val title = repo.getById(id)?.title ?: id
                    val confirmed = try {
                        confirmDelete(title)
                    } catch (_: Exception) {
                        false
                    }
                    if (!confirmed) {
                        val msg = "delete cancelled"
                        emit(ChatEvent.ToolActivity(msg))
                        remote.add(RemoteMessage(RemoteRole.Tool, toolCallId = call.id, name = wire, content = msg))
                        continue
                    }
                }
                val result = try {
                    registry.execute(toolName, args)
                } catch (e: Exception) {
                    "tool failed: ${e.message}"
                }
                trackIds(toolName, args, result, seenIds)
                emit(ChatEvent.ToolActivity(PromptBuilder.activityLine(toolName, result)))
                remote.add(RemoteMessage(RemoteRole.Tool, toolCallId = call.id, name = wire, content = result))
            }
        }
        emit(ChatEvent.Error("max turns reached"))
    }

    private fun parseId(args: String): String {
        return try {
            Json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject["id"]?.jsonPrimitive?.content.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun trackIds(tool: ToolName, args: String, result: String, seen: MutableSet<String>) {
        when (tool) {
            ToolName.SEARCH_NOTES -> {
                Regex("""[0-9a-fA-F-]{36}""").findAll(result).forEach { seen.add(it.value) }
            }
            ToolName.GET_NOTE -> {
                val id = parseId(args)
                if (id.isNotBlank()) seen.add(id)
            }
            ToolName.CREATE_NOTE -> {
                Regex("""[0-9a-fA-F-]{36}""").findAll(result).forEach { seen.add(it.value) }
            }
            ToolName.UPDATE_NOTE -> {
                val id = parseId(args)
                if (id.isNotBlank()) seen.add(id)
            }
            ToolName.DELETE_NOTE -> Unit
        }
    }

    companion object {
        const val MAX_TURNS = 5
    }
}
