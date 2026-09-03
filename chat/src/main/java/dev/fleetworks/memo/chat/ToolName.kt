package dev.fleetworks.memo.chat

enum class ToolName(val wire: String) {
    SEARCH_NOTES("search_notes"),
    GET_NOTE("get_note"),
    CREATE_NOTE("create_note"),
    UPDATE_NOTE("update_note"),
    DELETE_NOTE("delete_note");

    companion object {
        fun fromWire(wire: String): ToolName? = entries.find { it.wire == wire }
    }
}
