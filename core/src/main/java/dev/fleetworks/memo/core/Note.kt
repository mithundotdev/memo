package dev.fleetworks.memo.core

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    val tags: Set<String>
        get() = TagParser.extract("$title $body")
}
