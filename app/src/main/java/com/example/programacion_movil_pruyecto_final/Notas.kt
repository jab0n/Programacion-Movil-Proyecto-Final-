package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// --- Estructura de datos de ejemplo para una nota ---
// En un futuro, estos datos vendrán de una base de datos.
data class Nota(val id: Int, val titulo: String, val contenido: String)

// --- Lista de notas de ejemplo para visualizar el diseño ---
val notasDeEjemplo = listOf(
    Nota(1, "Idea para el proyecto", "Podríamos integrar una nueva función de búsqueda para que los usuarios encuentren sus notas más rápido."),
    Nota(2, "Lista de la compra", "Leche, huevos, pan y mantequilla. No olvidar el café."),
    Nota(3, "Recordatorio reunión", "Reunión importante con el equipo de diseño el viernes a las 10:00 AM para revisar los mockups."),
    Nota(4, "Libro recomendado", "Leer 'El Hábito Atómico' de James Clear. Parece muy interesante para mejorar la productividad."),
    Nota(5, "Contraseña Wifi", "NuevaContraseña123!"),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Notas(navController: NavHostController) {
    Scaffold(
        // Barra superior con título y botón para ir a Tareas
        topBar = {
            TopAppBar(
                title = { Text("Mis Notas") },
                actions = {
                    // Botón para navegar a la pantalla de Tareas
                    IconButton(onClick = { navController.navigate("tareas") }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Ir a Tareas"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // Botón flotante para añadir una nueva nota
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("crear_nota") }) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nota")
            }
        }
    ) { innerPadding ->
        // Contenido principal de la pantalla
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre las tarjetas
        ) {
            // "items" es una función de LazyColumn que crea un elemento por cada ítem en la lista.
            items(notasDeEjemplo) { nota ->
                NotaCard(nota = nota)
            }
        }
    }
}

@Composable
fun NotaCard(nota: Nota) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = nota.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Espacio vertical
            // Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = nota.contenido,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3, // Muestra un máximo de 3 líneas del contenido
                overflow = TextOverflow.Ellipsis // Añade "..." si el texto es muy largo
            )
        }
    }
}