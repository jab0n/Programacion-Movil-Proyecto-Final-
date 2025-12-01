package com.example.programacion_movil_pruyecto_final

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.programacion_movil_pruyecto_final.data.INotesRepository
import com.example.programacion_movil_pruyecto_final.data.ITasksRepository
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel

// Una fábrica de ViewModels que se encarga de crear instancias de los ViewModels de la aplicación.
class ViewModelFactory(
    private val application: Application,
    private val notesRepository: INotesRepository,
    private val tasksRepository: ITasksRepository
) : ViewModelProvider.Factory {
    // Este método se llama para crear una nueva instancia de un ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si la clase del modelo es asignable desde NotesViewModel.
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            // Si es así, crea y devuelve una instancia de NotesViewModel.
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(notesRepository) as T
        }
        // Comprueba si la clase del modelo es asignable desde TasksViewModel.
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            // Si es así, crea y devuelve una instancia de TasksViewModel.
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(application, tasksRepository) as T
        }
        // Si la clase del modelo no es conocida, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
