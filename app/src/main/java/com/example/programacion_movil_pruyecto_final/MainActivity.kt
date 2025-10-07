package com.example.programacion_movil_pruyecto_final

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController // Import the missing class
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.programacion_movil_pruyecto_final.ui.theme.ProgramacionMovilPruyectoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "notas") {
                composable("notas") { Notas(navController) }
                composable("tareas") { Tareas(navController) }
                composable("crear_nota") { CrearNota(navController) }
                composable("crear_tarea") { CrearTerea(navController) }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = "Hello $name!"
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProgramacionMovilPruyectoFinalTheme {
        Greeting("Android")
    }
}

