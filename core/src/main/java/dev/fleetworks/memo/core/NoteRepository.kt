package dev.fleetworks.memo.core

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    fun observeById(id: String): Flow<Note?>
    suspend fun search(query: String): List<Note>
    suspend fun getById(id: String): Note?
    suspend fun create(title: String, body: String): Note
    suspend fun update(id: String, title: String, body: String): Note?
    suspend fun delete(id: String): Boolean
    suspend fun backlinks(title: String): List<Note>
}
