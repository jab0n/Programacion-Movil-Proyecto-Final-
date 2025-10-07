package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun CrearNota(navController: NavHostController) {
    Column {
        Text("Crear Nota")
        Button(onClick = { navController.navigate("crear_tarea") }) {
            Text("Ir a Crear Tarea")
        }
    }
}