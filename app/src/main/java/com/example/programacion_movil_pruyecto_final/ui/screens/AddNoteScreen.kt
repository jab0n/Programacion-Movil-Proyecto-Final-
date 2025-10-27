package com.example.programacion_movil_pruyecto_final.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.programacion_movil_pruyecto_final.data.Note
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(application: NotesAndTasksApplication, onNoteAdded: () -> Unit) {
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Nota") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") }
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") }
            )
            Button(onClick = {
                viewModel.insert(Note(title = title, content = content))
                onNoteAdded()
            }) {
                Text("Guardar")
            }
        }
    }
}
