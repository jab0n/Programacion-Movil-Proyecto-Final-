package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

// Interfaz que define las operaciones del repositorio de tareas.
// Se utiliza para desacoplar la implementación del repositorio de su contrato.
interface ITasksRepository {
    // Flujo que emite la lista completa de tareas con sus adjuntos y recordatorios.
    val allTasks: Flow<List<TaskFull>>
    // Obtiene una tarea por su ID, junto con sus adjuntos y recordatorios.
    fun getTaskById(id: Int): Flow<TaskFull>
    // Inserta una tarea con sus adjuntos y recordatorios.
    suspend fun insert(task: Task, attachments: List<Attachment>, reminders: List<Reminder>)
    // Actualiza una tarea y sus recordatorios.
    suspend fun update(task: Task, reminders: List<Reminder>)
    // Elimina una tarea.
    suspend fun delete(task: Task)
    // Elimina un adjunto.
    suspend fun deleteAttachment(attachment: Attachment)
    // Inserta una lista de adjuntos.
    suspend fun insertAttachments(attachments: List<Attachment>)
}

// Implementación del repositorio de tareas. 
// Recibe una instancia de TaskDao para interactuar con la base de datos.
class TasksRepository(private val taskDao: TaskDao) : ITasksRepository {

    // Expone el flujo de todas las tareas desde el DAO.
    override val allTasks: Flow<List<TaskFull>> = taskDao.getAllTasks()

    // Obtiene una tarea específica por su ID desde el DAO.
    override fun getTaskById(id: Int): Flow<TaskFull> {
        return taskDao.getTaskById(id)
    }

    // Inserta una tarea y sus relaciones (adjuntos y recordatorios) utilizando el método transaccional del DAO.
    override suspend fun insert(task: Task, attachments: List<Attachment>, reminders: List<Reminder>) {
        taskDao.insertTaskWithRelations(task, attachments, reminders)
    }

    // Actualiza una tarea y sus recordatorios.
    override suspend fun update(task: Task, reminders: List<Reminder>) {
        taskDao.updateTask(task)
        taskDao.deleteRemindersByTaskId(task.id)
        taskDao.insertReminders(reminders.map { it.copy(taskId = task.id) })
    }

    // Elimina una tarea.
    override suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    // Elimina un adjunto.
    override suspend fun deleteAttachment(attachment: Attachment) {
        taskDao.deleteAttachment(attachment)
    }

    // Inserta una lista de adjuntos.
    override suspend fun insertAttachments(attachments: List<Attachment>) {
        taskDao.insertAttachments(attachments)
    }
}
