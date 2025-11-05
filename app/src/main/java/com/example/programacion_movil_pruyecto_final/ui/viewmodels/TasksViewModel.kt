package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.Task
import com.example.programacion_movil_pruyecto_final.data.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TasksUiState(
    val taskList: List<Task> = listOf()
)

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    val uiState: StateFlow<TasksUiState> = repository.allTasks
        .map { TasksUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TasksUiState()
        )

    fun getTaskById(id: Int) = repository.getTaskById(id)

    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}
