package com.example.programacion_movil_pruyecto_final.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_movil_pruyecto_final.data.*
import com.example.programacion_movil_pruyecto_final.notifications.TaskNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetails(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val isCompleted: Boolean = false,
    val attachments: List<Attachment> = emptyList(),
    val reminders: List<Reminder> = emptyList()
)

fun TaskFull.toTaskDetails(): TaskDetails = TaskDetails(
    id = task.id,
    title = task.title,
    content = task.content,
    isCompleted = task.isCompleted,
    attachments = attachments,
    reminders = reminders
)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    title = title,
    content = content,
    isCompleted = isCompleted
)

data class TasksUiState(
    val taskList: List<TaskFull> = listOf(),
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

    fun onCompletedChange(isCompleted: Boolean) {
        _taskDetails.update { it.copy(isCompleted = isCompleted) }
    }
    
    fun addReminder(date: String, time: String) {
        val newReminder = Reminder(taskId = _taskDetails.value.id, date = date, time = time)
        _taskDetails.update { it.copy(reminders = it.reminders + newReminder) }
    }

    fun onReminderEdited(oldReminder: Reminder, newDate: String, newTime: String) {
        val currentReminders = _taskDetails.value.reminders
        val index = currentReminders.indexOf(oldReminder)
        if (index != -1) {
            val updatedReminders = currentReminders.toMutableList()
            updatedReminders[index] = oldReminder.copy(date = newDate, time = newTime)
            _taskDetails.update { it.copy(reminders = updatedReminders) }
        }
    }

    fun removeReminder(reminder: Reminder) {
        _taskDetails.update { it.copy(reminders = it.reminders.filterNot { it == reminder }) }
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
        val reminders = _taskDetails.value.reminders

        repository.insert(task, attachments, reminders)
        
        val insertedTask = repository.allTasks.first().maxByOrNull { it.task.id } ?: return@launch
        if (!insertedTask.task.isCompleted) {
            scheduler.schedule(insertedTask.task, insertedTask.reminders)
        }
        clearTaskDetails()
    }

    private fun update() = viewModelScope.launch {
        val updatedTask = _taskDetails.value.toTask()
        val updatedReminders = _taskDetails.value.reminders
        val newAttachments = _newAttachments.value.map { (uri, type) ->
            Attachment(noteId = null, taskId = updatedTask.id, uri = uri.toString(), type = type ?: "")
        }

        val oldTaskFull = repository.getTaskById(updatedTask.id).firstOrNull()
        oldTaskFull?.let {
            scheduler.cancel(it.task, it.reminders)
        }

        repository.update(updatedTask, updatedReminders)
        if (newAttachments.isNotEmpty()) {
            repository.insertAttachments(newAttachments)
        }

        if (!updatedTask.isCompleted) {
            val finalTaskState = repository.getTaskById(updatedTask.id).firstOrNull()
            finalTaskState?.let {
                scheduler.schedule(it.task, it.reminders)
            }
        }
        
        clearTaskDetails()
    }

    fun update(task: Task, reminders: List<Reminder>) = viewModelScope.launch {
        scheduler.cancel(task, reminders)

        repository.update(task, reminders)

        if (!task.isCompleted) {
            val finalTaskState = repository.getTaskById(task.id).firstOrNull()
            finalTaskState?.let {
                scheduler.schedule(it.task, it.reminders)
            }
        }
    }

    fun delete(task: Task, reminders: List<Reminder>) = viewModelScope.launch {
        scheduler.cancel(task, reminders)
        repository.delete(task)
    }
}
