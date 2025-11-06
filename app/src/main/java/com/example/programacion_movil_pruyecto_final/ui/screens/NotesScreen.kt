package com.example.programacion_movil_pruyecto_final.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.Note
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NoteDetails
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    application: NotesAndTasksApplication, 
    onAddNote: () -> Unit, 
    isExpandedScreen: Boolean
) {
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
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
        if (isExpandedScreen) {
            Row(modifier = Modifier.padding(padding)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.noteList) { note ->
                        NoteItem(
                            note = note,
                            isExpanded = note.id in uiState.expandedNoteIds,
                            onClick = { viewModel.toggleNoteExpansion(note.id) },
                            onDelete = { viewModel.delete(note) },
                            onEdit = { viewModel.startEditingNote(note) }
                        )
                    }
                }
                if (uiState.isEditingNote) {
                    NoteDetailPanel(
                        modifier = Modifier.weight(1f),
                        noteDetails = uiState.noteDetails,
                        onDismiss = { viewModel.stopEditingNote() },
                        onConfirm = { viewModel.update() },
                        onTitleChange = viewModel::onTitleChange,
                        onContentChange = viewModel::onContentChange
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.noteList) { note ->
                    NoteItem(
                        note = note,
                        isExpanded = note.id in uiState.expandedNoteIds,
                        onClick = { viewModel.toggleNoteExpansion(note.id) },
                        onDelete = { viewModel.delete(note) },
                        onEdit = { viewModel.startEditingNote(note) }
                    )
                }
            }
            if (uiState.isEditingNote) {
                NoteDetailPanel(
                    isDialog = true,
                    noteDetails = uiState.noteDetails,
                    onDismiss = { viewModel.stopEditingNote() },
                    onConfirm = { viewModel.update() },
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange
                )
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, isExpanded: Boolean, onClick: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
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
                Text(text = note.content, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPanel(
    modifier: Modifier = Modifier,
    isDialog: Boolean = false,
    noteDetails: NoteDetails,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit
) {
    if (isDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.edit_note)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = noteDetails.title,
                        onValueChange = onTitleChange,
                        label = { Text(stringResource(R.string.title)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteDetails.content,
                        onValueChange = onContentChange,
                        label = { Text(stringResource(R.string.content)) },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    value = noteDetails.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteDetails.content,
                    onValueChange = onContentChange,
                    label = { Text(stringResource(R.string.content)) },
                    modifier = Modifier.fillMaxWidth().height(200.dp) // Maintain a reasonable default size
                )
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
