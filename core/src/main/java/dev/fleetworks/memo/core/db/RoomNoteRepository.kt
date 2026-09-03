package dev.fleetworks.memo.core.db

import dev.fleetworks.memo.core.FtsSentinel
import dev.fleetworks.memo.core.Note
import dev.fleetworks.memo.core.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomNoteRepository(private val db: MemoDatabase) : NoteRepository {
    private val dao: NoteDao = db.noteDao()

    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Note?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun search(query: String): List<Note> {
        val q = query.trim()
        if (q.isEmpty()) return dao.likeSearch("").map { it.toDomain() }
        val ftsHits = ftsSearch(q)
        if (ftsHits.isNotEmpty()) return ftsHits
        return dao.likeSearch(q).map { it.toDomain() }
    }

    override suspend fun getById(id: String): Note? =
        dao.getById(id)?.toDomain()

    override suspend fun create(title: String, body: String): Note {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(UUID.randomUUID().toString(), title, body, now, now)
        dao.insert(entity)
        ftsInsert(entity.id, title, body)
        return entity.toDomain()
    }

    override suspend fun update(id: String, title: String, body: String): Note? {
        val current = dao.getById(id) ?: return null
        val updated = current.copy(title = title, body = body, updatedAt = System.currentTimeMillis())
        dao.update(updated)
        ftsUpdate(id, title, body)
        return updated.toDomain()
    }

    override suspend fun delete(id: String): Boolean {
        ftsDelete(id)
        return dao.deleteById(id) > 0
    }

    override suspend fun backlinks(title: String): List<Note> {
        val raw = "[[$title]]"
        val ftsHits = ftsSearch(raw)
        if (ftsHits.isNotEmpty()) return ftsHits
        return dao.likeSearch(raw).map { it.toDomain() }
    }

    private suspend fun ftsSearch(rawQuery: String): List<Note> {
        val ids: List<String> = try {
            val transformed = FtsSentinel.forQuery(rawQuery)
            val cursor = db.openHelper.readableDatabase.query(
                "SELECT noteId FROM ${MemoDatabase.FTS_TABLE} WHERE ${MemoDatabase.FTS_TABLE} MATCH ?",
                arrayOf(transformed)
            )
            val out = mutableListOf<String>()
            cursor.use {
                while (it.moveToNext()) out.add(it.getString(0))
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return emptyList()
        return try {
            val rows = dao.getByIds(ids)
            val byId = rows.associateBy { it.id }
            ids.mapNotNull { byId[it]?.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun ftsInsert(noteId: String, title: String, body: String) {
        try {
            db.openHelper.writableDatabase.execSQL(
                "INSERT INTO ${MemoDatabase.FTS_TABLE}(noteId, title, body) VALUES (?, ?, ?)",
                arrayOf(noteId, FtsSentinel.forIndex(title), FtsSentinel.forIndex(body))
            )
        } catch (_: Exception) {
        }
    }

    private fun ftsUpdate(noteId: String, title: String, body: String) {
        try {
            db.openHelper.writableDatabase.execSQL(
                "UPDATE ${MemoDatabase.FTS_TABLE} SET title = ?, body = ? WHERE noteId = ?",
                arrayOf(FtsSentinel.forIndex(title), FtsSentinel.forIndex(body), noteId)
            )
        } catch (_: Exception) {
        }
    }

    private fun ftsDelete(noteId: String) {
        try {
            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM ${MemoDatabase.FTS_TABLE} WHERE noteId = ?",
                arrayOf(noteId)
            )
        } catch (_: Exception) {
        }
    }

    private fun NoteEntity.toDomain() = Note(id, title, body, createdAt, updatedAt)
}
