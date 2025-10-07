package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun Tareas(navController: NavHostController) {
    Column {
        Text("Tareas")
        Button(onClick = { navController.navigate("crear_nota") }) {
            Text("Ir a Crear Nota")
        }
    }
}