package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearNota(navController: NavHostController) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Nota") },
                // Botón para volver a la pantalla anterior
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón para "guardar" la nota
                    IconButton(onClick = {
                        // Aquí iría la lógica para guardar la nota
                        println("Nota guardada: Título: $titulo, Contenido: $contenido")
                        navController.popBackStack() // Vuelve a la pantalla de Notas
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Guardar Nota"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de texto para el título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                singleLine = true
            )

            // Campo de texto para el contenido de la nota
            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ocupa el espacio restante
                label = { Text("Contenido") }
            )
        }
    }
}
