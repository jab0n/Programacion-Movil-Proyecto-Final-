package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.Attachment
import com.example.programacion_movil_pruyecto_final.data.ITasksRepository
import com.example.programacion_movil_pruyecto_final.data.Task
import com.example.programacion_movil_pruyecto_final.data.TaskWithAttachments
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
    val isCompleted: Boolean = false,
    val attachments: List<Attachment> = emptyList()
)

fun TaskWithAttachments.toTaskDetails(): TaskDetails = TaskDetails(
    id = task.id,
    title = task.title,
    content = task.content,
    date = task.date,
    time = task.time,
    isCompleted = task.isCompleted,
    attachments = attachments
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
    val taskList: List<TaskWithAttachments> = listOf(),
    val taskDetails: TaskDetails = TaskDetails(),
    val isEditingTask: Boolean = false,
    val expandedTaskIds: Set<Int> = emptySet(),
    val newAttachments: MutableList<Pair<Uri, String?>> = mutableListOf()
)

class TasksViewModel(private val repository: ITasksRepository) : ViewModel() {

    private val _taskDetails = MutableStateFlow(TaskDetails())
    private val _isEditingTask = MutableStateFlow(false)
    private val _expandedTaskIds = MutableStateFlow(emptySet<Int>())
    private val _newAttachments = MutableStateFlow<MutableList<Pair<Uri, String?>>>(mutableListOf())

    val uiState: StateFlow<TasksUiState> = combine(
        repository.allTasks,
        _taskDetails,
        _isEditingTask,
        _expandedTaskIds,
        _newAttachments
    ) { tasks, details, isEditing, expandedIds, newAttachments ->
        TasksUiState(
            taskList = tasks,
            taskDetails = details,
            isEditingTask = isEditing,
            expandedTaskIds = expandedIds,
            newAttachments = newAttachments
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

    fun onAttachmentSelected(uri: Uri?, type: String?) {
        uri?.let { _newAttachments.value.add(it to type) }
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

    fun startEditingTask(task: TaskWithAttachments) {
        if (_isEditingTask.value && _taskDetails.value.id == task.task.id) {
            stopEditingTask()
        } else {
            _isEditingTask.value = true
            _taskDetails.value = task.toTaskDetails()
        }
    }

    fun stopEditingTask() {
        _isEditingTask.value = false
        clearTaskDetails()
    }

    fun clearTaskDetails() {
        _taskDetails.value = TaskDetails()
        _newAttachments.value.clear()
    }

    fun insert() = viewModelScope.launch {
        val attachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = null, taskId = 0, uri = uri.toString(), type = type ?: "")
        }
        repository.insert(_taskDetails.value.toTask(), attachments)
        clearTaskDetails()
    }

    fun update() = viewModelScope.launch {
        repository.update(_taskDetails.value.toTask())
        // TODO: Handle updating attachments
        stopEditingTask()
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}
