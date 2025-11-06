package com.example.programacion_movil_pruyecto_final.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.Task
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TaskDetails
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import java.util.Calendar
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
                    items(uiState.taskList) { task ->
                        TaskItem(
                            task = task,
                            isExpanded = task.id in uiState.expandedTaskIds,
                            onClick = { viewModel.toggleTaskExpansion(task.id) },
                            onDelete = { viewModel.delete(task) },
                            onEdit = { viewModel.startEditingTask(task) },
                            onCheckChange = { isChecked ->
                                viewModel.update(task.copy(isCompleted = isChecked))
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
                        onCompletedChange = viewModel::onCompletedChange
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.taskList) { task ->
                    TaskItem(
                        task = task,
                        isExpanded = task.id in uiState.expandedTaskIds,
                        onClick = { viewModel.toggleTaskExpansion(task.id) },
                        onDelete = { viewModel.delete(task) },
                        onEdit = { viewModel.startEditingTask(task) },
                        onCheckChange = { isChecked ->
                            viewModel.update(task.copy(isCompleted = isChecked))
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
                    onCompletedChange = viewModel::onCompletedChange
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task, 
    isExpanded: Boolean, 
    onClick: () -> Unit, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit, 
    onCheckChange: (Boolean) -> Unit
) {
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
                Text(text = task.content, modifier = Modifier.padding(top = 8.dp))
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
    onCompletedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    if (taskDetails.date.isNotEmpty()) {
        try {
            val parts = taskDetails.date.split("-").map { it.toInt() }
            if (parts.size == 3) {
                calendar.set(parts[0], parts[1] - 1, parts[2])
            }
        } catch (e: Exception) { /* Ignore parsing errors for old formats */ }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            onDateChange("%d-%02d-%02d".format(year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            onTimeChange("%02d:%02d".format(hourOfDay, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val displayDate = remember(taskDetails.date) {
                            val parts = taskDetails.date.split("-")
                            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else taskDetails.date
                        }
                        Button(onClick = { datePickerDialog.show() }) {
                            Text(text = displayDate.ifEmpty { stringResource(R.string.select_date) })
                        }
                        Button(onClick = { timePickerDialog.show() }) {
                            Text(text = taskDetails.time.ifEmpty { stringResource(R.string.select_time) })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = taskDetails.isCompleted, onCheckedChange = onCompletedChange)
                        Text(text = stringResource(R.string.completed))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val displayDate = remember(taskDetails.date) {
                        val parts = taskDetails.date.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else taskDetails.date
                    }
                    Button(onClick = { datePickerDialog.show() }) {
                        Text(text = displayDate.ifEmpty { stringResource(R.string.select_date) })
                    }
                    Button(onClick = { timePickerDialog.show() }) {
                        Text(text = taskDetails.time.ifEmpty { stringResource(R.string.select_time) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = taskDetails.isCompleted, onCheckedChange = onCompletedChange)
                    Text(text = stringResource(R.string.completed))
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
