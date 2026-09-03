package dev.fleetworks.memo.chat

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage as RemoteMessage
import com.aallam.openai.api.chat.ChatRole as RemoteRole
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.api.core.Parameters
import dev.fleetworks.memo.core.profile.ProviderProfile
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class OpenAiChatSource(profile: ProviderProfile) {
    private val model = ModelId(profile.model.ifBlank { "gpt-4o-mini" })
    private val client = OpenAI(
        OpenAIConfig(
            host = OpenAIHost(baseUrl = profile.baseUrl.ifBlank { DEFAULT_BASE_URL }),
            token = profile.apiKey
        )
    )

    suspend fun complete(messages: List<RemoteMessage>): RemoteMessage {
        val request = chatCompletionRequest {
            this.model = this@OpenAiChatSource.model
            this.messages = messages
            tools {
                for (def in toolDefs()) {
                    function(name = def.name, description = def.description, parameters = Parameters(def.parameters))
                }
            }
            toolChoice = ToolChoice.Auto
        }
        return client.chatCompletion(request).choices.first().message
    }

    suspend fun test(): Boolean {
        client.models()
        return true
    }

    suspend fun rawComplete(request: ChatCompletionRequest) =
        client.chatCompletion(request)

    private fun toolDefs(): List<ToolDef> = listOf(
        ToolDef(
            "search_notes", "Search notes by keyword",
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("query") { put("type", "string") }
                    putJsonObject("limit") { put("type", "integer") }
                }
            }
        ),
        ToolDef(
            "get_note", "Read one note by id",
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("id") { put("type", "string") }
                }
                putJsonArray("required") { add(JsonPrimitive("id")) }
            }
        ),
        ToolDef(
            "create_note", "Create a note",
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("title") { put("type", "string") }
                    putJsonObject("body") { put("type", "string") }
                }
            }
        ),
        ToolDef(
            "update_note", "Update a note by id",
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("id") { put("type", "string") }
                    putJsonObject("title") { put("type", "string") }
                    putJsonObject("body") { put("type", "string") }
                }
                putJsonArray("required") { add(JsonPrimitive("id")) }
            }
        ),
        ToolDef(
            "delete_note", "Delete a note by id",
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("id") { put("type", "string") }
                }
                putJsonArray("required") { add(JsonPrimitive("id")) }
            }
        )
    )

    fun systemMessage(): RemoteMessage =
        RemoteMessage(role = RemoteRole.System, content = PromptBuilder.system())

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1/"
    }

    private data class ToolDef(
        val name: String,
        val description: String,
        val parameters: kotlinx.serialization.json.JsonObject
    )
}
