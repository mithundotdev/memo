package dev.fleetworks.memo.chat

import dev.fleetworks.memo.core.NoteRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

typealias ToolHandler = suspend (argsJson: String) -> String

class ToolRegistry(val handlers: Map<ToolName, ToolHandler>) {
    suspend fun execute(name: ToolName, argsJson: String): String =
        handlers[name]?.invoke(argsJson) ?: "unknown tool ${name.wire}"

    companion object {
        fun create(repo: NoteRepository): ToolRegistry {
            val json = Json { ignoreUnknownKeys = true }
            return ToolRegistry(
                mapOf(
                    ToolName.SEARCH_NOTES to { args ->
                        val obj = json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject
                        val query = obj["query"]?.jsonPrimitive?.content ?: ""
                        val limit = obj["limit"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5
                        val hits = repo.search(query).take(limit)
                        if (hits.isEmpty()) "found 0 notes"
                        else "found ${hits.size} notes: " + hits.joinToString("; ") { "${it.id}|${it.title}" }
                    },
                    ToolName.GET_NOTE to { args ->
                        val obj = json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject
                        val id = obj["id"]?.jsonPrimitive?.content.orEmpty()
                        val note = repo.getById(id)
                        if (note == null) "note not found"
                        else "opened ${note.title}: ${note.body.take(500)}"
                    },
                    ToolName.CREATE_NOTE to { args ->
                        val obj = json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject
                        val title = obj["title"]?.jsonPrimitive?.content.orEmpty()
                        val body = obj["body"]?.jsonPrimitive?.content.orEmpty()
                        val note = repo.create(title.ifBlank { "Untitled" }, body)
                        "created ${note.title} ${note.id}"
                    },
                    ToolName.UPDATE_NOTE to { args ->
                        val obj = json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject
                        val id = obj["id"]?.jsonPrimitive?.content.orEmpty()
                        val title = obj["title"]?.jsonPrimitive?.content.orEmpty()
                        val body = obj["body"]?.jsonPrimitive?.content.orEmpty()
                        val note = repo.update(id, title, body)
                        if (note == null) "note not found" else "updated ${note.title}"
                    },
                    ToolName.DELETE_NOTE to { args ->
                        val obj = json.parseToJsonElement(args.ifBlank { "{}" }).jsonObject
                        val id = obj["id"]?.jsonPrimitive?.content.orEmpty()
                        val ok = repo.delete(id)
                        if (ok) "deleted $id" else "note not found"
                    }
                )
            )
        }
    }
}
