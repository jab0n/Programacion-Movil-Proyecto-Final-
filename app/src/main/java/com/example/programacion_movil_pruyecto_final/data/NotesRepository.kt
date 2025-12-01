package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

// Interfaz que define las operaciones del repositorio de notas.
// Se utiliza para desacoplar la implementación del repositorio de su contrato.
interface INotesRepository {
    // Flujo que emite la lista completa de notas con sus adjuntos.
    val allNotes: Flow<List<NoteWithAttachments>>
    // Obtiene una nota por su ID, junto con sus adjuntos.
    fun getNoteById(id: Int): Flow<NoteWithAttachments>
    // Inserta una nota con sus adjuntos.
    suspend fun insert(note: Note, attachments: List<Attachment>)
    // Actualiza una nota.
    suspend fun update(note: Note)
    // Elimina una nota.
    suspend fun delete(note: Note)
    // Elimina un adjunto.
    suspend fun deleteAttachment(attachment: Attachment)
    // Inserta una lista de adjuntos.
    suspend fun insertAttachments(attachments: List<Attachment>)
}

// Implementación del repositorio de notas. 
// Recibe una instancia de NoteDao para interactuar con la base de datos.
class NotesRepository(private val noteDao: NoteDao) : INotesRepository {

    // Expone el flujo de todas las notas desde el DAO.
    override val allNotes: Flow<List<NoteWithAttachments>> = noteDao.getAllNotes()

    // Obtiene una nota específica por su ID desde el DAO.
    override fun getNoteById(id: Int): Flow<NoteWithAttachments> {
        return noteDao.getNoteById(id)
    }

    // Inserta una nota y sus adjuntos utilizando el método transaccional del DAO.
    override suspend fun insert(note: Note, attachments: List<Attachment>) {
        noteDao.insertNoteWithAttachments(note, attachments)
    }

    // Actualiza una nota existente.
    override suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    // Elimina una nota.
    override suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }

    // Elimina un adjunto.
    override suspend fun deleteAttachment(attachment: Attachment) {
        noteDao.deleteAttachment(attachment)
    }

    // Inserta una lista de adjuntos.
    override suspend fun insertAttachments(attachments: List<Attachment>) {
        noteDao.insertAttachments(attachments)
    }
}
