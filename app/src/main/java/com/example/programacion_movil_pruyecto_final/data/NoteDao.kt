package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<Attachment>)

    @Transaction
    suspend fun insertNoteWithAttachments(note: Note, attachments: List<Attachment>) {
        val noteId = insertNote(note)
        val attachmentsWithNoteId = attachments.map { it.copy(noteId = noteId.toInt()) }
        insertAttachments(attachmentsWithNoteId)
    }

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<NoteWithAttachments>

    @Transaction
    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getAllNotes(): Flow<List<NoteWithAttachments>>
}
