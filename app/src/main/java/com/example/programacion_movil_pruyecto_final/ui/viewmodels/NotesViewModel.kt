package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.INotesRepository
import com.example.programacion_movil_pruyecto_final.data.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteDetails(
    val id: Int = 0,
    val title: String = "",
    val content: String = ""
)

fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    title = title,
    content = content
)

fun NoteDetails.toNote(): Note = Note(
    id = id,
    title = title,
    content = content
)

data class NotesUiState(
    val noteList: List<Note> = listOf(),
    val noteDetails: NoteDetails = NoteDetails(),
    val isEditingNote: Boolean = false,
    val expandedNoteIds: Set<Int> = emptySet()
)

class NotesViewModel(private val repository: INotesRepository) : ViewModel() {

    private val _noteDetails = MutableStateFlow(NoteDetails())
    private val _isEditingNote = MutableStateFlow(false)
    private val _expandedNoteIds = MutableStateFlow(emptySet<Int>())

    val uiState: StateFlow<NotesUiState> = combine(
        repository.allNotes,
        _noteDetails,
        _isEditingNote,
        _expandedNoteIds
    ) { notes, details, isEditing, expandedIds ->
        NotesUiState(
            noteList = notes,
            noteDetails = details,
            isEditingNote = isEditing,
            expandedNoteIds = expandedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun onTitleChange(title: String) {
        _noteDetails.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _noteDetails.update { it.copy(content = content) }
    }

    fun toggleNoteExpansion(noteId: Int) {
        _expandedNoteIds.update { currentIds ->
            if (noteId in currentIds) {
                currentIds - noteId
            } else {
                currentIds + noteId
            }
        }
    }

    fun startEditingNote(note: Note) {
        _isEditingNote.value = true
        _noteDetails.value = note.toNoteDetails()
    }

    fun stopEditingNote() {
        _isEditingNote.value = false
        clearNoteDetails()
    }

    fun clearNoteDetails() {
        _noteDetails.value = NoteDetails()
    }

    fun insert() = viewModelScope.launch {
        repository.insert(_noteDetails.value.toNote())
        clearNoteDetails()
    }

    fun update() = viewModelScope.launch {
        repository.update(_noteDetails.value.toNote())
        stopEditingNote()
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
