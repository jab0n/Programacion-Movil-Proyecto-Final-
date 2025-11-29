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
import kotlinx.coroutines.flow.firstOrNull
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
    val expandedNoteIds: Set<Int> = emptySet(),
    val newAttachments: List<Pair<Uri, String?>> = emptyList()
)

class NotesViewModel(private val repository: INotesRepository) : ViewModel() {

    private val _noteDetails = MutableStateFlow(NoteDetails())
    private val _expandedNoteIds = MutableStateFlow(emptySet<Int>())
    private val _newAttachments = MutableStateFlow<List<Pair<Uri, String?>>>(emptyList())

    val uiState: StateFlow<NotesUiState> = combine(
        repository.allNotes,
        _noteDetails,
        _expandedNoteIds,
        _newAttachments
    ) { notes, details, expandedIds, newAttachments ->
        NotesUiState(
            noteList = notes,
            noteDetails = details,
            expandedNoteIds = expandedIds,
            newAttachments = newAttachments
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun prepareForEntry(noteId: Int?) {
        val currentId = _noteDetails.value.id
        if (noteId != null && noteId != currentId) {
            loadNote(noteId)
        } else if (noteId == null && currentId != 0) {
            clearNoteDetails()
        }
    }

    private fun loadNote(noteId: Int) {
        viewModelScope.launch {
            repository.getNoteById(noteId).firstOrNull()?.let {
                _noteDetails.value = it.toNoteDetails()
            }
        }
    }

    fun onTitleChange(title: String) {
        _noteDetails.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _noteDetails.update { it.copy(content = content) }
    }

    fun onAttachmentSelected(uri: Uri?, type: String?) {
        uri?.let { newUri ->
            val isAlreadyInNew = _newAttachments.value.any { it.first == newUri }
            val isAlreadyInExisting = _noteDetails.value.attachments.any { it.uri == newUri.toString() }

            if (!isAlreadyInNew && !isAlreadyInExisting) {
                _newAttachments.update { currentList -> currentList + (newUri to type) }
            }
        }
    }

    fun removeAttachment(uri: Uri) {
        _newAttachments.update { currentList ->
            currentList.filterNot { it.first == uri }
        }
    }

    fun removeExistingAttachment(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            _noteDetails.update { 
                it.copy(attachments = it.attachments.filterNot { it.id == attachment.id }) 
            }
        }
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

    fun clearNoteDetails() {
        _noteDetails.value = NoteDetails()
        _newAttachments.value = emptyList()
    }

    fun save() {
        if (_noteDetails.value.id == 0) {
            insert()
        } else {
            update()
        }
    }

    private fun insert() = viewModelScope.launch {
        val note = _noteDetails.value.toNote()
        val attachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = 0, taskId = null, uri = uri.toString(), type = type ?: "")
        }
        repository.insert(note, attachments)
        clearNoteDetails()
    }

    private fun update() = viewModelScope.launch {
        val note = _noteDetails.value.toNote()
        val newAttachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = _noteDetails.value.id, taskId = null, uri = uri.toString(), type = type ?: "")
        }
        repository.update(note)
        repository.insertAttachments(newAttachments)
        clearNoteDetails()
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
