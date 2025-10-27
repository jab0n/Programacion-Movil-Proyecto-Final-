package com.example.programacion_movil_pruyecto_final

import android.app.Application
import com.example.programacion_movil_pruyecto_final.data.AppDatabase
import com.example.programacion_movil_pruyecto_final.data.NotesRepository
import com.example.programacion_movil_pruyecto_final.data.TasksRepository

class NotesAndTasksApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val notesRepository by lazy { NotesRepository(database.noteDao()) }
    val tasksRepository by lazy { TasksRepository(database.taskDao()) }
}
