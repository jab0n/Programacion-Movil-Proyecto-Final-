package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

interface ITasksRepository {
    val allTasks: Flow<List<TaskFull>>
    fun getTaskById(id: Int): Flow<TaskFull>
    suspend fun insert(task: Task, attachments: List<Attachment>, reminders: List<Reminder>)
    suspend fun update(task: Task, reminders: List<Reminder>)
    suspend fun delete(task: Task)
    suspend fun deleteAttachment(attachment: Attachment)
    suspend fun insertAttachments(attachments: List<Attachment>)
}

class TasksRepository(private val taskDao: TaskDao) : ITasksRepository {

    override val allTasks: Flow<List<TaskFull>> = taskDao.getAllTasks()

    override fun getTaskById(id: Int): Flow<TaskFull> {
        return taskDao.getTaskById(id)
    }

    override suspend fun insert(task: Task, attachments: List<Attachment>, reminders: List<Reminder>) {
        taskDao.insertTaskWithRelations(task, attachments, reminders)
    }

    override suspend fun update(task: Task, reminders: List<Reminder>) {
        taskDao.updateTask(task)
        taskDao.deleteRemindersByTaskId(task.id)
        taskDao.insertReminders(reminders.map { it.copy(taskId = task.id) })
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
