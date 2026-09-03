package dev.fleetworks.memo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.core.Note
import dev.fleetworks.memo.core.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SaveState { Saving, Saved }

class NoteDetailViewModel(private val repo: NoteRepository, private val noteId: String) : ViewModel() {
    val title = MutableStateFlow("")
    val body = MutableStateFlow("")
    val backlinks = MutableStateFlow<List<Note>>(emptyList())
    val saveState = MutableStateFlow(SaveState.Saved)
    private var currentId: String? = noteId.takeIf { it != "new" && it.isNotBlank() }
    private var lastSaved = "" to ""
    private var watcher: Job? = null

    fun load() {
        val id = currentId ?: return
        viewModelScope.launch {
            val note = repo.getById(id) ?: return@launch
            title.value = note.title
            body.value = note.body
            lastSaved = note.title to note.body
            backlinks.value = repo.backlinks(note.title)
        }
    }

    fun startAutoSave() {
        watcher?.cancel()
        watcher = viewModelScope.launch {
            combine(title, body) { t, b -> t to b }.collect { (t, b) ->
                if (t to b == lastSaved) return@collect
                if (t.isBlank() && b.isBlank()) return@collect
                saveState.value = SaveState.Saving
                delay(700)
                if (t to b != (title.value to body.value)) return@collect
                persist(t, b)
                lastSaved = t to b
                saveState.value = SaveState.Saved
            }
        }
    }

    private suspend fun persist(t: String, b: String) {
        val id = currentId
        if (id == null) {
            currentId = repo.create(t, b).id
        } else {
            repo.update(id, t, b)
        }
    }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val t = title.value
            val b = body.value
            persist(t, b)
            lastSaved = t to b
            onSaved(currentId.orEmpty())
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            currentId?.let { repo.delete(it) }
            onDone()
        }
    }

    fun current(): StateFlow<String> = title
}
