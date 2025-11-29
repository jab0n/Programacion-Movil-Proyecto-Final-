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
import com.example.programacion_movil_pruyecto_final.data.NoteWithAttachments
import com.example.programacion_movil_pruyecto_final.utils.getFileName
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    application: NotesAndTasksApplication, 
    onAddNote: () -> Unit, 
    onEditNote: (Int) -> Unit,
    onAttachmentClick: (String, String) -> Unit,
    isExpandedScreen: Boolean
) {
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.notes)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.noteList) { noteWithAttachments ->
                NoteItem(
                    noteWithAttachments = noteWithAttachments,
                    isExpanded = noteWithAttachments.note.id in uiState.expandedNoteIds,
                    onClick = { viewModel.toggleNoteExpansion(noteWithAttachments.note.id) },
                    onDelete = { viewModel.delete(noteWithAttachments.note) },
                    onEdit = { onEditNote(noteWithAttachments.note.id) },
                    onAttachmentClick = onAttachmentClick
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    noteWithAttachments: NoteWithAttachments, 
    isExpanded: Boolean, 
    onClick: () -> Unit, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit,
    onAttachmentClick: (String, String) -> Unit
) {
    val note = noteWithAttachments.note
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = note.title, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_note))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Text(text = note.content, modifier = Modifier.padding(top = 8.dp))
                    noteWithAttachments.attachments.forEach { attachment ->
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
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.no_app_found),
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = getFileName(context, Uri.parse(attachment.uri)))
                        }
                    }
                }
            }
        }
    }
}
