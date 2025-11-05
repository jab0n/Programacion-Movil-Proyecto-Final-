package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

interface INotesRepository {
    val allNotes: Flow<List<Note>>
    fun getNoteById(id: Int): Flow<Note>
    suspend fun insert(note: Note)
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
}

class NotesRepository(private val noteDao: NoteDao) : INotesRepository {

    override val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    override fun getNoteById(id: Int): Flow<Note> {
        return noteDao.getNoteById(id)
    }

    override suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    override suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }
}
