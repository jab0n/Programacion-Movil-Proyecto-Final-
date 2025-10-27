package com.example.programacion_movil_pruyecto_final.data

import kotlinx.coroutines.flow.Flow

class TasksRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTaskById(id: Int): Flow<Task> {
        return taskDao.getTaskById(id)
    }

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}
