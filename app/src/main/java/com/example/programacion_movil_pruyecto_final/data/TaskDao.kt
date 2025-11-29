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
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<Attachment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    @Transaction
    suspend fun insertTaskWithRelations(task: Task, attachments: List<Attachment>, reminders: List<Reminder>) {
        val taskId = insertTask(task)
        val attachmentsWithTaskId = attachments.map { it.copy(taskId = taskId.toInt()) }
        val remindersWithTaskId = reminders.map { it.copy(taskId = taskId.toInt()) }
        insertAttachments(attachmentsWithTaskId)
        insertReminders(remindersWithTaskId)
    }

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersByTaskId(taskId: Int)

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Int): Flow<TaskFull>

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY id DESC") // Order by id as date is removed
    fun getAllTasks(): Flow<List<TaskFull>>
}
