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

// Clase de datos que representa los detalles de una nota.
data class NoteDetails(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val attachments: List<Attachment> = emptyList()
)

// Función de extensión para convertir un objeto NoteWithAttachments a NoteDetails.
fun NoteWithAttachments.toNoteDetails(): NoteDetails = NoteDetails(
    id = note.id,
    title = note.title,
    content = note.content,
    attachments = attachments
)

// Función de extensión para convertir un objeto NoteDetails a Note.
fun NoteDetails.toNote(): Note = Note(
    id = id,
    title = title,
    content = content
)

// Clase de datos que representa el estado de la UI para la pantalla de notas.
data class NotesUiState(
    val noteList: List<NoteWithAttachments> = listOf(),
    val noteDetails: NoteDetails = NoteDetails(),
    val expandedNoteIds: Set<Int> = emptySet(),
    val newAttachments: List<Pair<Uri, String?>> = emptyList()
)

// ViewModel para la pantalla de notas.
class NotesViewModel(private val repository: INotesRepository) : ViewModel() {

    // Flujos de estado privados para gestionar los detalles de la nota, las notas expandidas y los nuevos adjuntos.
    private val _noteDetails = MutableStateFlow(NoteDetails())
    private val _expandedNoteIds = MutableStateFlow(emptySet<Int>())
    private val _newAttachments = MutableStateFlow<List<Pair<Uri, String?>>>(emptyList())

    // Combina varios flujos para crear el estado de la UI (NotesUiState).
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

    // Prepara el ViewModel para la entrada de una nota, ya sea cargando una existente o limpiando los detalles.
    fun prepareForEntry(noteId: Int?) {
        val currentId = _noteDetails.value.id
        if (noteId != null && noteId != currentId) {
            loadNote(noteId)
        } else if (noteId == null && currentId != 0) {
            clearNoteDetails()
        }
    }

    // Carga los detalles de una nota por su ID.
    private fun loadNote(noteId: Int) {
        viewModelScope.launch {
            repository.getNoteById(noteId).firstOrNull()?.let {
                _noteDetails.value = it.toNoteDetails()
            }
        }
    }

    // Actualiza el título de la nota.
    fun onTitleChange(title: String) {
        _noteDetails.update { it.copy(title = title) }
    }

    // Actualiza el contenido de la nota.
    fun onContentChange(content: String) {
        _noteDetails.update { it.copy(content = content) }
    }

    // Añade un nuevo adjunto a la nota.
    fun onAttachmentSelected(uri: Uri?, type: String?) {
        uri?.let { newUri ->
            val isAlreadyInNew = _newAttachments.value.any { it.first == newUri }
            val isAlreadyInExisting = _noteDetails.value.attachments.any { it.uri == newUri.toString() }

            if (!isAlreadyInNew && !isAlreadyInExisting) {
                _newAttachments.update { currentList -> currentList + (newUri to type) }
            }
        }
    }

    // Elimina un nuevo adjunto de la lista de adjuntos temporales.
    fun removeAttachment(uri: Uri) {
        _newAttachments.update { currentList ->
            currentList.filterNot { it.first == uri }
        }
    }

    // Elimina un adjunto existente de la base de datos.
    fun removeExistingAttachment(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            _noteDetails.update { 
                it.copy(attachments = it.attachments.filterNot { it.id == attachment.id }) 
            }
        }
    }

    // Expande o contrae una nota en la lista.
    fun toggleNoteExpansion(noteId: Int) {
        _expandedNoteIds.update { currentIds ->
            if (noteId in currentIds) {
                currentIds - noteId
            } else {
                currentIds + noteId
            }
        }
    }

    // Limpia los detalles de la nota y la lista de nuevos adjuntos.
    fun clearNoteDetails() {
        _noteDetails.value = NoteDetails()
        _newAttachments.value = emptyList()
    }

    // Guarda la nota (inserta una nueva o actualiza una existente).
    fun save() {
        if (_noteDetails.value.id == 0) {
            insert()
        } else {
            update()
        }
    }

    // Inserta una nueva nota con sus adjuntos.
    private fun insert() = viewModelScope.launch {
        val note = _noteDetails.value.toNote()
        val attachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = 0, taskId = null, uri = uri.toString(), type = type ?: "")
        }
        repository.insert(note, attachments)
        clearNoteDetails()
    }

    // Actualiza una nota existente y añade los nuevos adjuntos.
    private fun update() = viewModelScope.launch {
        val note = _noteDetails.value.toNote()
        val newAttachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = _noteDetails.value.id, taskId = null, uri = uri.toString(), type = type ?: "")
        }
        repository.update(note)
        repository.insertAttachments(newAttachments)
        clearNoteDetails()
    }

    // Elimina una nota.
    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
