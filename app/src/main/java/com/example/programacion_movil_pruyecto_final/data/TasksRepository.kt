package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

interface ITasksRepository {
    val allTasks: Flow<List<TaskWithAttachments>>
    fun getTaskById(id: Int): Flow<TaskWithAttachments>
    suspend fun insert(task: Task, attachments: List<Attachment>)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
    suspend fun deleteAttachment(attachment: Attachment)
    suspend fun insertAttachments(attachments: List<Attachment>)
}

class TasksRepository(private val taskDao: TaskDao) : ITasksRepository {

    override val allTasks: Flow<List<TaskWithAttachments>> = taskDao.getAllTasks()

    override fun getTaskById(id: Int): Flow<TaskWithAttachments> {
        return taskDao.getTaskById(id)
    }

    override suspend fun insert(task: Task, attachments: List<Attachment>) {
        taskDao.insertTaskWithAttachments(task, attachments)
    }

    override suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    override suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        taskDao.deleteAttachment(attachment)
    }

    override suspend fun insertAttachments(attachments: List<Attachment>) {
        taskDao.insertAttachments(attachments)
    }
}
