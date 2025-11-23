package com.example.programacion_movil_pruyecto_final.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.TaskWithAttachments
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TaskDetails
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    application: NotesAndTasksApplication, 
    onAddTask: () -> Unit,
    isExpandedScreen: Boolean
) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tasks)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        if (isExpandedScreen) {
            Row(modifier = Modifier.padding(padding)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.taskList) { taskWithAttachments ->
                        TaskItem(
                            taskWithAttachments = taskWithAttachments,
                            isExpanded = taskWithAttachments.task.id in uiState.expandedTaskIds,
                            onClick = { viewModel.toggleTaskExpansion(taskWithAttachments.task.id) },
                            onDelete = { viewModel.delete(taskWithAttachments.task) },
                            onEdit = { viewModel.startEditingTask(taskWithAttachments) },
                            onCheckChange = { isChecked ->
                                viewModel.update(taskWithAttachments.task.copy(isCompleted = isChecked))
                            }
                        )
                    }
                }
                if (uiState.isEditingTask) {
                    TaskDetailPanel(
                        modifier = Modifier.weight(1f),
                        taskDetails = uiState.taskDetails,
                        onDismiss = { viewModel.stopEditingTask() },
                        onConfirm = { viewModel.update() },
                        onTitleChange = viewModel::onTitleChange,
                        onContentChange = viewModel::onContentChange,
                        onDateChange = viewModel::onDateChange,
                        onTimeChange = viewModel::onTimeChange,
                        onCompletedChange = viewModel::onCompletedChange,
                        onAttachmentSelected = viewModel::onAttachmentSelected
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.taskList) { taskWithAttachments ->
                    TaskItem(
                        taskWithAttachments = taskWithAttachments,
                        isExpanded = taskWithAttachments.task.id in uiState.expandedTaskIds,
                        onClick = { viewModel.toggleTaskExpansion(taskWithAttachments.task.id) },
                        onDelete = { viewModel.delete(taskWithAttachments.task) },
                        onEdit = { viewModel.startEditingTask(taskWithAttachments) },
                        onCheckChange = { isChecked ->
                            viewModel.update(taskWithAttachments.task.copy(isCompleted = isChecked))
                        }
                    )
                }
            }
            if (uiState.isEditingTask) {
                TaskDetailPanel(
                    isDialog = true,
                    taskDetails = uiState.taskDetails,
                    onDismiss = { viewModel.stopEditingTask() },
                    onConfirm = { viewModel.update() },
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                    onDateChange = viewModel::onDateChange,
                    onTimeChange = viewModel::onTimeChange,
                    onCompletedChange = viewModel::onCompletedChange,
                    onAttachmentSelected = viewModel::onAttachmentSelected
                )
            }
        }
    }
}

@Composable
fun TaskItem(taskWithAttachments: TaskWithAttachments, isExpanded: Boolean, onClick: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit, onCheckChange: (Boolean) -> Unit) {
    val task = taskWithAttachments.task
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckChange
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = task.title)
                    val dateParts = task.date.split("-")
                    val displayDate = if (dateParts.size == 3) "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}" else task.date
                    Text(text = "$displayDate ${task.time}")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_task))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Text(text = task.content, modifier = Modifier.padding(top = 8.dp))
                    taskWithAttachments.attachments.forEach { attachment ->
                        Spacer(modifier = Modifier.height(8.dp))
                        if (attachment.type.startsWith("image/")) {
                            AsyncImage(
                                model = attachment.uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Text(text = attachment.uri.substringAfterLast("/"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailPanel(
    modifier: Modifier = Modifier,
    isDialog: Boolean = false,
    taskDetails: TaskDetails,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onAttachmentSelected: (Uri?, String?) -> Unit
) {
    val context = LocalContext.current

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val type = uri?.let { context.contentResolver.getType(it) }
        onAttachmentSelected(uri, type)
    }

    if (isDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.edit_task)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = taskDetails.title,
                        onValueChange = onTitleChange,
                        label = { Text(stringResource(R.string.title)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskDetails.content,
                        onValueChange = onContentChange,
                        label = { Text(stringResource(R.string.content)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { getContent.launch("*/*") }) {
                        Text(text = stringResource(R.string.attach_file))
                    }
                    taskDetails.attachments.forEach { attachment ->
                        Text(text = attachment.uri.substringAfterLast("/"))
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    } else {
        Column(
            modifier = modifier.padding(16.dp).fillMaxHeight()
        ) {
            // Scrollable content area
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = taskDetails.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskDetails.content,
                    onValueChange = onContentChange,
                    label = { Text(stringResource(R.string.content)) },
                    modifier = Modifier.fillMaxWidth().height(200.dp) // Maintain a reasonable default size
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { getContent.launch("*/*") }) {
                    Text(text = stringResource(R.string.attach_file))
                }
                 taskDetails.attachments.forEach { attachment ->
                    Text(text = attachment.uri.substringAfterLast("/"))
                }
            }
            // Sticky action buttons at the bottom
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onConfirm) {
                    Text(stringResource(R.string.save))
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
