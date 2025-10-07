package com.example.programacion_movil_pruyecto_final

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.programacion_movil_pruyecto_final.Notas
import com.example.programacion_movil_pruyecto_final.Tareas
import com.example.programacion_movil_pruyecto_final.CrearNota
import com.example.programacion_movil_pruyecto_final.CrearTarea

@Composable
fun CrearNota(navController: NavHostController) {
    Column {
        Text("Crear Nota")
        Button(onClick = { navController.navigate("crear_tarea") }) {
            Text("Ir a Crear Tarea")
        }
    }
}