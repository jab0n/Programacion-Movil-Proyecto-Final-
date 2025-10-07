package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun CrearTerea(navController: NavHostController) {
    Column {
        Text("Crear Tarea")
        Button(onClick = { navController.navigate("notas") }) {
            Text("Ir a Notas")
        }
    }
}