package dev.fleetworks.memo.chat

import dev.fleetworks.memo.core.Note
import dev.fleetworks.memo.core.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRepo : NoteRepository {
    val notes = mutableMapOf<String, Note>()
    var counter = 0

    override fun observeAll(): Flow<List<Note>> = flowOf(notes.values.toList())
    override fun observeById(id: String): Flow<Note?> = flowOf(notes[id])
    override suspend fun search(query: String): List<Note> {
        if (query.isBlank()) return notes.values.toList()
        return notes.values.filter { it.title.contains(query, true) || it.body.contains(query, true) }
    }
    override suspend fun getById(id: String): Note? = notes[id]
    override suspend fun create(title: String, body: String): Note {
        val id = "00000000-0000-4000-8000-${counter.toString().padStart(12, '0')}"
        counter += 1
        val note = Note(id, title, body, 1L, 1L)
        notes[id] = note
        return note
    }
    override suspend fun update(id: String, title: String, body: String): Note? {
        val current = notes[id] ?: return null
        val updated = current.copy(title = title, body = body)
        notes[id] = updated
        return updated
    }
    override suspend fun delete(id: String): Boolean = notes.remove(id) != null
    override suspend fun backlinks(title: String): List<Note> =
        notes.values.filter { it.body.contains("[[$title]]") }
}

class ToolRegistryTest {
    @Test
    fun searchCreateGetUpdateDelete() = runTest {
        val repo = FakeRepo()
        val registry = ToolRegistry.create(repo)

        val created = registry.execute(ToolName.CREATE_NOTE, """{"title":"Shopping","body":"milk"}""")
        assertTrue(created.contains("created Shopping"))

        val searched = registry.execute(ToolName.SEARCH_NOTES, """{"query":"Shop","limit":5}""")
        assertTrue(searched.contains("found 1 notes"))

        val id = repo.notes.keys.first()
        val opened = registry.execute(ToolName.GET_NOTE, """{"id":"$id"}""")
        assertTrue(opened.contains("Shopping"))

        val updated = registry.execute(ToolName.UPDATE_NOTE, """{"id":"$id","title":"Shopping","body":"milk eggs"}""")
        assertEquals("updated Shopping", updated)

        val deleted = registry.execute(ToolName.DELETE_NOTE, """{"id":"$id"}""")
        assertEquals("deleted $id", deleted)
    }

    @Test
    fun unknownIdReturnsNotFound() = runTest {
        val registry = ToolRegistry.create(FakeRepo())
        assertEquals("note not found", registry.execute(ToolName.GET_NOTE, """{"id":"missing"}"""))
    }
}
