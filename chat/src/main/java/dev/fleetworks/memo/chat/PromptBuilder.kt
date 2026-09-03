package dev.fleetworks.memo.chat

object PromptBuilder {
    fun system(): String =
        "You are Memo, an offline first notes assistant. " +
            "Use tools to search, read, create, update, and delete notes. " +
            "Always search before reading or deleting an id. " +
            "Summarize tool results briefly. " +
            "Never invent note ids."

    fun activityLine(tool: ToolName, result: String): String =
        when (tool) {
            ToolName.SEARCH_NOTES -> summarizeSearch(result)
            ToolName.GET_NOTE -> "opened 1 note"
            ToolName.CREATE_NOTE -> "created 1 note"
            ToolName.UPDATE_NOTE -> "updated 1 note"
            ToolName.DELETE_NOTE -> "deleted 1 note"
        }

    private fun summarizeSearch(result: String): String {
        val count = Regex("""found (\d+) notes""").find(result)?.groupValues?.get(1) ?: "0"
        return "searched $count notes"
    }
}
