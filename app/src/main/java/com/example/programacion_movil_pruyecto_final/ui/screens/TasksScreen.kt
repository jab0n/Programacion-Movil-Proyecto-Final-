package com.example.programacion_movil_pruyecto_final.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.Task
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(application: NotesAndTasksApplication, onAddTask: () -> Unit) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
    val tasks by viewModel.allTasks.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tareas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(tasks) {
                TaskItem(
                    task = it,
                    onDelete = { viewModel.delete(it) },
                    onEdit = { 
                        taskToEdit = it
                        showEditDialog = true
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
fun TaskItem(task: Task, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { /* No se actualiza aquí */ }
            )
            Text(text = task.title, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar tarea")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(task: Task, onDismiss: () -> Unit, onConfirm: (Task) -> Unit) {
    var title by remember { mutableStateOf(task.title) }
    var isCompleted by remember { mutableStateOf(task.isCompleted) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") }
                )
                Row {
                    Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
                    Text(text = "Completada")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(task.copy(title = title, isCompleted = isCompleted))
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
