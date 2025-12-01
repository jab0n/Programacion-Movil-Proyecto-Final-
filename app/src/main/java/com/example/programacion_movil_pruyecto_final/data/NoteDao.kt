package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Anotación que marca la interfaz como un DAO (Data Access Object) de Room.
@Dao
interface NoteDao {

    // Inserta una nota en la base de datos. 
    // OnConflictStrategy.REPLACE indica que si se inserta una nota con un ID que ya existe, se reemplazará.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    // Inserta una lista de adjuntos en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<Attachment>)

    // Anotación que asegura que las operaciones dentro del método se ejecuten en una única transacción.
    @Transaction
    suspend fun insertNoteWithAttachments(note: Note, attachments: List<Attachment>) {
        val noteId = insertNote(note)
        val attachmentsWithNoteId = attachments.map { it.copy(noteId = noteId.toInt()) }
        insertAttachments(attachmentsWithNoteId)
    }

    // Actualiza una nota existente en la base de datos.
    @Update
    suspend fun updateNote(note: Note)

    // Elimina una nota de la base de datos.
    @Delete
    suspend fun deleteNote(note: Note)

    // Elimina un adjunto de la base de datos.
    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    // Obtiene una nota por su ID, junto con sus adjuntos.
    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<NoteWithAttachments>

    // Obtiene todas las notas ordenadas por título, junto con sus adjuntos.
    @Transaction
    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getAllNotes(): Flow<List<NoteWithAttachments>>
}
