package dev.fleetworks.memo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fleetworks.memo.core.Note
import dev.fleetworks.memo.core.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(repo: NoteRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val activeTag = MutableStateFlow<String?>(null)

    private val all = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = combine(all, query, activeTag) { list, q, tag ->
        var out = list
        if (q.isNotBlank()) out = out.filter { it.title.contains(q, true) || it.body.contains(q, true) }
        if (tag != null) out = out.filter { tag in it.tags }
        out
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<Set<String>> = combine(all) { arr ->
        arr[0].flatMap { it.tags }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
}
