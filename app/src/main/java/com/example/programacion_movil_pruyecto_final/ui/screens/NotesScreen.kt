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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.NoteWithAttachments
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel
import com.example.programacion_movil_pruyecto_final.utils.getFileName

// Composable que representa la pantalla principal de notas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    application: NotesAndTasksApplication,
    onAddNote: () -> Unit, // Lógica para añadir una nueva nota.
    onEditNote: (Int) -> Unit, // Lógica para editar una nota existente.
    onAttachmentClick: (String, String) -> Unit, // Lógica para manejar el clic en un adjunto.
    isExpandedScreen: Boolean // Indica si la pantalla es expandida (para tablets o dispositivos grandes).
) {
    // Obtiene una instancia del ViewModel para esta pantalla.
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    // Obtiene el estado de la UI desde el ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // Estructura de la pantalla utilizando Scaffold.
    Scaffold(
        topBar = {
            // Barra de la aplicación en la parte superior.
            TopAppBar(
                title = { Text(stringResource(R.string.notes)) }
            )
        },
        floatingActionButton = {
            // Botón de acción flotante para añadir una nueva nota.
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note))
            }
        }
    ) { padding ->
        // Lista de notas que ocupa el resto de la pantalla.
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(8.dp) // Añade un padding alrededor de la lista.
        ) {
            // Itera sobre la lista de notas y muestra cada una como un NoteItem.
            items(uiState.noteList) { noteWithAttachments ->
                NoteItem(
                    noteWithAttachments = noteWithAttachments,
                    isExpanded = noteWithAttachments.note.id in uiState.expandedNoteIds, // Si la nota está expandida.
                    onClick = { viewModel.toggleNoteExpansion(noteWithAttachments.note.id) }, // Lógica para expandir/contraer la nota.
                    onDelete = { viewModel.delete(noteWithAttachments.note) }, // Lógica para eliminar la nota.
                    onEdit = { onEditNote(noteWithAttachments.note.id) }, // Lógica para editar la nota.
                    onAttachmentClick = onAttachmentClick
                )
            }
        }
    }
}

// Composable que representa un único elemento de nota en la lista.
@Composable
fun NoteItem(
    noteWithAttachments: NoteWithAttachments,
    isExpanded: Boolean, // Si la nota está expandida.
    onClick: () -> Unit, // Lógica para manejar el clic en el item.
    onDelete: () -> Unit, // Lógica para eliminar el item.
    onEdit: () -> Unit, // Lógica para editar el item.
    onAttachmentClick: (String, String) -> Unit // Lógica para manejar el clic en un adjunto.
) {
    val note = noteWithAttachments.note
    val context = LocalContext.current

    // Tarjeta que contiene la información de la nota.
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
                Text(text = note.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                // Botones de acción para editar y eliminar la nota.
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_note))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            // Contenido expandible que se muestra solo si la nota está expandida.
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Text(text = note.content, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium)
                    // Itera sobre los adjuntos y los muestra.
                    noteWithAttachments.attachments.forEach { attachment ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val attachmentType = attachment.type ?: ""
                                    // Si el adjunto es una imagen, video o audio, se abre en el visor de medios.
                                    if (attachmentType.startsWith("image/") ||
                                        attachmentType.startsWith("video/") ||
                                        attachmentType.startsWith("audio/")
                                    ) {
                                        onAttachmentClick(attachment.uri, attachmentType)
                                    } else {
                                        // Si es otro tipo de archivo, se intenta abrir con una aplicación externa.
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
                            // Muestra una miniatura si el adjunto es una imagen o video.
                            if (attachmentType.startsWith("image/") || attachmentType.startsWith("video/")) {
                                AsyncImage(
                                    model = attachment.uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Muestra un icono de archivo adjunto genérico.
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
