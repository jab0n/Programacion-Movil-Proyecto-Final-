package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.Attachment
import com.example.programacion_movil_pruyecto_final.data.INotesRepository
import com.example.programacion_movil_pruyecto_final.data.Note
import com.example.programacion_movil_pruyecto_final.data.NoteWithAttachments
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
    val content: String = "",
    val attachments: List<Attachment> = emptyList()
)

fun NoteWithAttachments.toNoteDetails(): NoteDetails = NoteDetails(
    id = note.id,
    title = note.title,
    content = note.content,
    attachments = attachments
)

fun NoteDetails.toNote(): Note = Note(
    id = id,
    title = title,
    content = content
)

data class NotesUiState(
    val noteList: List<NoteWithAttachments> = listOf(),
    val noteDetails: NoteDetails = NoteDetails(),
    val isEditingNote: Boolean = false,
    val expandedNoteIds: Set<Int> = emptySet(),
    val newAttachments: MutableList<Pair<Uri, String?>> = mutableListOf()
)

class NotesViewModel(private val repository: INotesRepository) : ViewModel() {

    private val _noteDetails = MutableStateFlow(NoteDetails())
    private val _isEditingNote = MutableStateFlow(false)
    private val _expandedNoteIds = MutableStateFlow(emptySet<Int>())
    private val _newAttachments = MutableStateFlow<MutableList<Pair<Uri, String?>>>(mutableListOf())

    val uiState: StateFlow<NotesUiState> = combine(
        repository.allNotes,
        _noteDetails,
        _isEditingNote,
        _expandedNoteIds,
        _newAttachments
    ) { notes, details, isEditing, expandedIds, newAttachments ->
        NotesUiState(
            noteList = notes,
            noteDetails = details,
            isEditingNote = isEditing,
            expandedNoteIds = expandedIds,
            newAttachments = newAttachments
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

    fun onAttachmentSelected(uri: Uri?, type: String?) {
        uri?.let { _newAttachments.value.add(it to type) }
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

    fun startEditingNote(note: NoteWithAttachments) {
        if (_isEditingNote.value && _noteDetails.value.id == note.note.id) {
            stopEditingNote()
        } else {
            _isEditingNote.value = true
            _noteDetails.value = note.toNoteDetails()
        }
    }

    fun stopEditingNote() {
        _isEditingNote.value = false
        clearNoteDetails()
    }

    fun clearNoteDetails() {
        _noteDetails.value = NoteDetails()
        _newAttachments.value.clear()
    }

    fun insert() = viewModelScope.launch {
        val attachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = 0, taskId = null, uri = uri.toString(), type = type ?: "")
        }
        repository.insert(_noteDetails.value.toNote(), attachments)
        clearNoteDetails()
    }

    fun update() = viewModelScope.launch {
        repository.update(_noteDetails.value.toNote())
        // TODO: Handle updating attachments
        stopEditingNote()
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
