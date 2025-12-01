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
interface TaskDao {

    // Inserta una tarea en la base de datos. 
    // OnConflictStrategy.REPLACE indica que si se inserta una tarea con un ID que ya existe, se reemplazará.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    // Inserta una lista de adjuntos en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<Attachment>)

    // Inserta una lista de recordatorios en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    // Anotación que asegura que las operaciones dentro del método se ejecuten en una única transacción.
    @Transaction
    suspend fun insertTaskWithRelations(task: Task, attachments: List<Attachment>, reminders: List<Reminder>) {
        val taskId = insertTask(task)
        val attachmentsWithTaskId = attachments.map { it.copy(taskId = taskId.toInt()) }
        val remindersWithTaskId = reminders.map { it.copy(taskId = taskId.toInt()) }
        insertAttachments(attachmentsWithTaskId)
        insertReminders(remindersWithTaskId)
    }

    // Actualiza una tarea existente en la base de datos.
    @Update
    suspend fun updateTask(task: Task)

    // Elimina una tarea de la base de datos.
    @Delete
    suspend fun deleteTask(task: Task)

    // Elimina un adjunto de la base de datos.
    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    // Elimina todos los recordatorios asociados a una tarea por su ID.
    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersByTaskId(taskId: Int)

    // Obtiene una tarea por su ID, junto con sus adjuntos y recordatorios.
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Int): Flow<TaskFull>

    // Obtiene todas las tareas ordenadas por ID descendente, junto con sus adjuntos y recordatorios.
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskFull>>
}
