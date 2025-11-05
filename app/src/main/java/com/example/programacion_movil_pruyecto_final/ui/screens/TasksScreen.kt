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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(application: NotesAndTasksApplication, onAddTask: () -> Unit) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

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
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.taskList) {
                var isExpanded by remember { mutableStateOf(false) }
                TaskItem(
                    task = it,
                    isExpanded = isExpanded,
                    onExpand = { isExpanded = !isExpanded },
                    onDelete = { viewModel.delete(it) },
                    onEdit = { 
                        taskToEdit = it
                        showEditDialog = true
                    },
                    onCheckChange = { isChecked ->
                        viewModel.update(it.copy(isCompleted = isChecked))
                    }
                )
            }
        }
    }

    if (showEditDialog) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { 
                viewModel.update(it)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TaskItem(task: Task, isExpanded: Boolean, onExpand: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit, onCheckChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onExpand() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
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
fun EditTaskDialog(task: Task, onDismiss: () -> Unit, onConfirm: (Task) -> Unit) {
    var title by remember { mutableStateOf(task.title) }
    var content by remember { mutableStateOf(task.content) }
    var date by remember { mutableStateOf(task.date) } // Se guarda como YYYY-MM-DD
    var time by remember { mutableStateOf(task.time) }
    var isCompleted by remember { mutableStateOf(task.isCompleted) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Configurar el calendario con la fecha existente si está en el formato correcto
    if (date.isNotEmpty()) {
        try {
            val parts = date.split("-").map { it.toInt() }
            if (parts.size == 3) {
                calendar.set(parts[0], parts[1] - 1, parts[2])
            }
        } catch (e: Exception) { /* Ignorar errores de formato antiguo */ }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Guardar en formato ordenable
            date = "%d-%02d-%02d".format(year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            time = "%02d:%02d".format(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_task)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) }
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.content)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Formatear para mostrar al usuario
                    val displayDate = remember(date) {
                        val parts = date.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
                    }
                    Button(onClick = { datePickerDialog.show() }) {
                        Text(text = displayDate.ifEmpty { stringResource(R.string.select_date) })
                    }
                    Button(onClick = { timePickerDialog.show() }) {
                        Text(text = time.ifEmpty { stringResource(R.string.select_time) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
                    Text(text = stringResource(R.string.completed))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(task.copy(title = title, content = content, isCompleted = isCompleted, date = date, time = time))
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
