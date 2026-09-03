package dev.fleetworks.memo.core

object NoteStats {
    fun wordCount(text: String): Int =
        text.split(Regex("\\s+")).count { it.isNotBlank() }
}
