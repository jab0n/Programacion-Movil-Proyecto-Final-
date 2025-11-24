package com.example.programacion_movil_pruyecto_final.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.TaskWithAttachments
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    application: NotesAndTasksApplication,
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    isExpandedScreen: Boolean
) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
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
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.taskList) { taskWithAttachments ->
                TaskItem(
                    taskWithAttachments = taskWithAttachments,
                    isExpanded = taskWithAttachments.task.id in uiState.expandedTaskIds,
                    onClick = { viewModel.toggleTaskExpansion(taskWithAttachments.task.id) },
                    onDelete = { viewModel.delete(taskWithAttachments.task) },
                    onEdit = { onEditTask(taskWithAttachments.task.id) },
                    onCheckChange = { isChecked ->
                        viewModel.update(taskWithAttachments.task.copy(isCompleted = isChecked))
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    taskWithAttachments: TaskWithAttachments,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onCheckChange: (Boolean) -> Unit
) {
    val task = taskWithAttachments.task
    val context = LocalContext.current

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { 
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val uri = Uri.parse(attachment.uri)
                                    setDataAndType(uri, attachment.type)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
                                }
                            }.padding(vertical = 4.dp)
                        ) {
                            if (attachment.type.startsWith("image/")) {
                                AsyncImage(
                                    model = attachment.uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = attachment.uri.substringAfterLast("/"))
                        }
                    }
                }
            }
        }
    }
}
