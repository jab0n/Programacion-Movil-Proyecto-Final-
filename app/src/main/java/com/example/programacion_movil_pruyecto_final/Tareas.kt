package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Estructura de datos y lista de ejemplo para Tareas
data class Tarea(val id: Int, val descripcion: String, val completada: Boolean = false)

val tareasDeEjemplo = listOf(
    Tarea(1, "Terminar el diseño de la app de notas"),
    Tarea(2, "Implementar la base de datos local", completada = true),
    Tarea(3, "Hacer la compra semanal"),
    Tarea(4, "Llamar al dentista para pedir cita"),
    Tarea(5, "Preparar la presentación del lunes", completada = true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tareas(navController: NavHostController) {
    // Usamos 'remember' para que el estado de las tareas persista mientras la pantalla esté activa
    var tareas by remember { mutableStateOf(tareasDeEjemplo) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    // Botón para navegar a la pantalla de Notas
                    IconButton(onClick = { navController.navigate("notas") }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Ir a Notas"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("crear_tarea") }) {
                Icon(Icons.Default.Add, contentDescription = "Crear Tarea")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tareas) { tarea ->
                TareaCard(
                    tarea = tarea,
                    onCheckedChange = { isChecked ->
                        // Actualizamos el estado de la tarea en la lista
                        tareas = tareas.map {
                            if (it.id == tarea.id) it.copy(completada = isChecked) else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TareaCard(tarea: Tarea, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tarea.descripcion,
                modifier = Modifier.weight(1f),
                // Tacha el texto si la tarea está completada
                textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                color = if (tarea.completada) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
