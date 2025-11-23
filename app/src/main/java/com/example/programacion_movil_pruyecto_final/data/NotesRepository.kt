package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

interface INotesRepository {
    val allNotes: Flow<List<NoteWithAttachments>>
    fun getNoteById(id: Int): Flow<NoteWithAttachments>
    suspend fun insert(note: Note, attachments: List<Attachment>)
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun deleteAttachment(attachment: Attachment)
    suspend fun insertAttachments(attachments: List<Attachment>)
}

class NotesRepository(private val noteDao: NoteDao) : INotesRepository {

    override val allNotes: Flow<List<NoteWithAttachments>> = noteDao.getAllNotes()

    override fun getNoteById(id: Int): Flow<NoteWithAttachments> {
        return noteDao.getNoteById(id)
    }

    override suspend fun insert(note: Note, attachments: List<Attachment>) {
        noteDao.insertNoteWithAttachments(note, attachments)
    }

    override suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        noteDao.deleteAttachment(attachment)
    }

    override suspend fun insertAttachments(attachments: List<Attachment>) {
        noteDao.insertAttachments(attachments)
    }
}
