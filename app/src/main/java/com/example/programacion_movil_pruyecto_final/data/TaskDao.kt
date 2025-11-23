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

    @Transaction
    suspend fun insertTaskWithAttachments(task: Task, attachments: List<Attachment>) {
        val taskId = insertTask(task)
        val attachmentsWithTaskId = attachments.map { it.copy(taskId = taskId.toInt()) }
        insertAttachments(attachmentsWithTaskId)
    }

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Int): Flow<TaskWithAttachments>

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    fun getAllTasks(): Flow<List<TaskWithAttachments>>
}
