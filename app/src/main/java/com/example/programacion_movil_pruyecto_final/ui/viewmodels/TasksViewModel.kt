package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.Attachment
import com.example.programacion_movil_pruyecto_final.data.ITasksRepository
import com.example.programacion_movil_pruyecto_final.data.Task
import com.example.programacion_movil_pruyecto_final.data.TaskWithAttachments
import com.example.programacion_movil_pruyecto_final.notifications.TaskNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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
    val expandedTaskIds: Set<Int> = emptySet(),
    val newAttachments: List<Pair<Uri, String?>> = emptyList()
)

class TasksViewModel(application: Application, private val repository: ITasksRepository) : AndroidViewModel(application) {

    private val scheduler = TaskNotificationScheduler(application)

    private val _taskDetails = MutableStateFlow(TaskDetails())
    private val _expandedTaskIds = MutableStateFlow(emptySet<Int>())
    private val _newAttachments = MutableStateFlow<List<Pair<Uri, String?>>>(emptyList())

    val uiState: StateFlow<TasksUiState> = combine(
        repository.allTasks,
        _taskDetails,
        _expandedTaskIds,
        _newAttachments
    ) { tasks, details, expandedIds, newAttachments ->
        TasksUiState(
            taskList = tasks,
            taskDetails = details,
            expandedTaskIds = expandedIds,
            newAttachments = newAttachments
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun prepareForEntry(taskId: Int?) {
        val currentId = _taskDetails.value.id
        if (taskId != null && taskId != currentId) {
            loadTaskForEditing(taskId)
        } else if (taskId == null && currentId != 0) {
            clearTaskDetails()
        }
    }

    private fun loadTaskForEditing(taskId: Int) {
        viewModelScope.launch {
            repository.getTaskById(taskId).firstOrNull()?.let { 
                _taskDetails.value = it.toTaskDetails()
            }
        }
    }

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
        uri?.let { 
            _newAttachments.update { currentList -> currentList + (it to type) }
        }
    }

    fun removeAttachment(uri: Uri) {
        _newAttachments.update { currentList ->
            currentList.filterNot { it.first == uri }
        }
    }

    fun removeExistingAttachment(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            _taskDetails.update { 
                it.copy(attachments = it.attachments.filterNot { it.id == attachment.id }) 
            }
        }
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

    fun clearTaskDetails() {
        _taskDetails.value = TaskDetails()
        _newAttachments.value = emptyList()
    }

    fun save() {
        if (_taskDetails.value.id == 0) {
            insert()
        } else {
            update()
        }
    }

    private fun insert() = viewModelScope.launch {
        val task = _taskDetails.value.toTask()
        val attachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = null, taskId = 0, uri = uri.toString(), type = type ?: "")
        }
        repository.insert(task, attachments)
        if (!task.isCompleted) scheduler.schedule(task)
        clearTaskDetails()
    }

    private fun update() = viewModelScope.launch {
        val task = _taskDetails.value.toTask()
        val newAttachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = null, taskId = _taskDetails.value.id, uri = uri.toString(), type = type ?: "")
        }
        repository.update(task)
        repository.insertAttachments(newAttachments)
        if (!task.isCompleted) {
            scheduler.schedule(task)
        } else {
            scheduler.cancel(task.id)
        }
        clearTaskDetails()
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
         if (!task.isCompleted) {
            scheduler.schedule(task)
        } else {
            scheduler.cancel(task.id)
        }
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
        scheduler.cancel(task.id)
    }
}
