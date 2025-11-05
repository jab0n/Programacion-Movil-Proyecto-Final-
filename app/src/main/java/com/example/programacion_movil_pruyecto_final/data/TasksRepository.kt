package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

interface ITasksRepository {
    val allTasks: Flow<List<Task>>
    fun getTaskById(id: Int): Flow<Task>
    suspend fun insert(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
}

class TasksRepository(private val taskDao: TaskDao) : ITasksRepository {

    override val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTaskById(id: Int): Flow<Task> {
        return taskDao.getTaskById(id)
    }

    override suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    override suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}
