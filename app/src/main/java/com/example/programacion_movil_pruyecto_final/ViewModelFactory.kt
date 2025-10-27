package com.example.programacion_movil_pruyecto_final

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.programacion_movil_pruyecto_final.data.NotesRepository
import com.example.programacion_movil_pruyecto_final.data.TasksRepository
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel

class ViewModelFactory(private val notesRepository: NotesRepository, private val tasksRepository: TasksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(notesRepository) as T
        }
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(tasksRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
