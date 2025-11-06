package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.ITasksRepository
import com.example.programacion_movil_pruyecto_final.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetails(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val time: String = "",
    val isCompleted: Boolean = false
)

fun Task.toTaskDetails(): TaskDetails = TaskDetails(
    id = id,
    title = title,
    content = content,
    date = date,
    time = time,
    isCompleted = isCompleted
)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    title = title,
    content = content,
    date = date,
    time = time,
    isCompleted = isCompleted
)

data class TasksUiState(
    val taskList: List<Task> = listOf(),
    val taskDetails: TaskDetails = TaskDetails(),
    val isEditingTask: Boolean = false,
    val expandedTaskIds: Set<Int> = emptySet()
)

class TasksViewModel(private val repository: ITasksRepository) : ViewModel() {

    private val _taskDetails = MutableStateFlow(TaskDetails())
    private val _isEditingTask = MutableStateFlow(false)
    private val _expandedTaskIds = MutableStateFlow(emptySet<Int>())

    val uiState: StateFlow<TasksUiState> = combine(
        repository.allTasks,
        _taskDetails,
        _isEditingTask,
        _expandedTaskIds
    ) { tasks, details, isEditing, expandedIds ->
        TasksUiState(
            taskList = tasks,
            taskDetails = details,
            isEditingTask = isEditing,
            expandedTaskIds = expandedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun onTitleChange(title: String) {
        _taskDetails.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _taskDetails.update { it.copy(content = content) }
    }

    fun onDateChange(date: String) {
        _taskDetails.update { it.copy(date = date) }
    }

    fun onTimeChange(time: String) {
        _taskDetails.update { it.copy(time = time) }
    }

    fun onCompletedChange(isCompleted: Boolean) {
        _taskDetails.update { it.copy(isCompleted = isCompleted) }
    }
    
    fun toggleTaskExpansion(taskId: Int) {
        _expandedTaskIds.update { currentIds ->
            if (taskId in currentIds) {
                currentIds - taskId
            } else {
                currentIds + taskId
            }
        }
    }

    fun startEditingTask(task: Task) {
        _isEditingTask.value = true
        _taskDetails.value = task.toTaskDetails()
    }

    fun stopEditingTask() {
        _isEditingTask.value = false
        clearTaskDetails()
    }

    fun clearTaskDetails() {
        _taskDetails.value = TaskDetails()
    }

    fun insert() = viewModelScope.launch {
        repository.insert(_taskDetails.value.toTask())
        clearTaskDetails()
    }

    fun update() = viewModelScope.launch {
        repository.update(_taskDetails.value.toTask())
        stopEditingTask()
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}
