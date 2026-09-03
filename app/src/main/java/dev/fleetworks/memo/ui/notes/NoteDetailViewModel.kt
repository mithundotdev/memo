package dev.fleetworks.memo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.core.Note
import dev.fleetworks.memo.core.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repo: NoteRepository, private val noteId: String) : ViewModel() {
    val title = MutableStateFlow("")
    val body = MutableStateFlow("")
    val backlinks = MutableStateFlow<List<Note>>(emptyList())
    private val loadedId = MutableStateFlow<String?>(null)

    fun load() {
        if (noteId == "new" || noteId.isBlank()) return
        if (loadedId.value == noteId) return
        loadedId.value = noteId
        viewModelScope.launch {
            val note = repo.getById(noteId) ?: return@launch
            title.value = note.title
            body.value = note.body
            backlinks.value = repo.backlinks(note.title)
        }
    }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (noteId == "new" || noteId.isBlank()) {
                val created = repo.create(title.value, body.value)
                onSaved(created.id)
            } else {
                repo.update(noteId, title.value, body.value)
                onSaved(noteId)
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.delete(noteId)
            onDone()
        }
    }

    fun current(): StateFlow<String> = title
}
