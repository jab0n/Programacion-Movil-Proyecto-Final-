package com.example.programacion_movil_pruyecto_final.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.TaskFull
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import com.example.programacion_movil_pruyecto_final.utils.getFileName

// Composable que representa la pantalla principal de tareas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    application: NotesAndTasksApplication,
    onAddTask: () -> Unit, // Lógica para añadir una nueva tarea.
    onEditTask: (Int) -> Unit, // Lógica para editar una tarea existente.
    onAttachmentClick: (String, String) -> Unit, // Lógica para manejar el clic en un adjunto.
    isExpandedScreen: Boolean // Indica si la pantalla es expandida (para tablets o dispositivos grandes).
) {
    // Obtiene una instancia del ViewModel para esta pantalla.
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    // Obtiene el estado de la UI desde el ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // Estructura de la pantalla utilizando Scaffold.
    Scaffold(
        topBar = {
            // Barra de la aplicación en la parte superior.
            TopAppBar(
                title = { Text(stringResource(R.string.tasks)) }
            )
        },
        floatingActionButton = {
            // Botón de acción flotante para añadir una nueva tarea.
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        // Lista de tareas que ocupa el resto de la pantalla.
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(8.dp) // Añade un padding alrededor de la lista.
        ) {
            // Itera sobre la lista de tareas y muestra cada una como un TaskItem.
            items(uiState.taskList) { taskFull ->
                TaskItem(
                    taskFull = taskFull,
                    isExpanded = taskFull.task.id in uiState.expandedTaskIds, // Si la tarea está expandida.
                    onClick = { viewModel.toggleTaskExpansion(taskFull.task.id) }, // Lógica para expandir/contraer la tarea.
                    onDelete = { viewModel.delete(taskFull.task, taskFull.reminders) }, // Lógica para eliminar la tarea.
                    onEdit = { onEditTask(taskFull.task.id) }, // Lógica para editar la tarea.
                    onCheckChange = { isChecked ->
                        viewModel.update(taskFull.task.copy(isCompleted = isChecked), taskFull.reminders)
                    }, // Lógica para marcar la tarea como completada.
                    onAttachmentClick = onAttachmentClick
                )
            }
        }
    }
}

// Composable que representa un único elemento de tarea en la lista.
@Composable
fun TaskItem(
    taskFull: TaskFull,
    isExpanded: Boolean, // Si la tarea está expandida.
    onClick: () -> Unit, // Lógica para manejar el clic en el item.
    onDelete: () -> Unit, // Lógica para eliminar el item.
    onEdit: () -> Unit, // Lógica para editar el item.
    onCheckChange: (Boolean) -> Unit, // Lógica para marcar la tarea como completada.
    onAttachmentClick: (String, String) -> Unit // Lógica para manejar el clic en un adjunto.
) {
    val task = taskFull.task
    val context = LocalContext.current
    // Estilo del texto que cambia si la tarea está completada.
    val textStyle = if (task.isCompleted) {
        MaterialTheme.typography.titleLarge.copy(textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    } else {
        MaterialTheme.typography.titleLarge
    }

    // Tarjeta que contiene la información de la tarea.
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Añade elevación para dar profundidad.
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = textStyle,
                )
                // Botones de acción para editar y eliminar la tarea.
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_task))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            // Contenido expandible que se muestra solo si la tarea está expandida.
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = task.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))

                    // Muestra los recordatorios si existen.
                    if (taskFull.reminders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        taskFull.reminders.forEach { reminder ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Reminder", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${reminder.date} at ${reminder.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    // Itera sobre los adjuntos y los muestra.
                    taskFull.attachments.forEach { attachment ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val attachmentType = attachment.type ?: ""
                                    if (attachmentType.startsWith("image/") ||
                                        attachmentType.startsWith("video/") ||
                                        attachmentType.startsWith("audio/")
                                    ) {
                                        onAttachmentClick(attachment.uri, attachmentType)
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            val uri = Uri.parse(attachment.uri)
                                            setDataAndType(uri, attachmentType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, context.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            val attachmentType = attachment.type ?: ""
                            if (attachmentType.startsWith("image/") || attachmentType.startsWith("video/")) {
                                AsyncImage(
                                    model = attachment.uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = getFileName(context, Uri.parse(attachment.uri)), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
