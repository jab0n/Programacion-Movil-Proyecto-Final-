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

            NavHost(navController = navController, startDestination = "pantalla1") {
                composable("pantalla1") { PantallaUno(navController) }
                composable("pantalla2") { PantallaDos(navController) }
                composable("pantalla3") { PantallaTres(navController) }
                composable("pantalla4") { PantallaCuatro(navController) }
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


@Composable
fun PantallaUno(navController: NavHostController) {
    Column {
        Text("Pantalla Uno")
        Button(onClick = { navController.navigate("pantalla2") }) {
            Text("Ir a Pantalla Dos")
        }
    }
}

@Composable
fun PantallaDos(navController: NavHostController) {
    Column {
        Text("Pantalla Dos")
        Button(onClick = { navController.navigate("pantalla3") }) {
            Text("Ir a Pantalla Tres")
        }
    }
}

@Composable
fun PantallaTres(navController: NavHostController) {
    Column {
        Text("Pantalla Tres")
        Button(onClick = { navController.navigate("pantalla4") }) {
            Text("Ir a Pantalla Cuatro")
        }
    }
}

@Composable
fun PantallaCuatro(navController: NavHostController) {
    Column {
        Text("Pantalla Cuatro")
        Button(onClick = { navController.popBackStack("pantalla1", inclusive = false) }) {
            Text("Volver a Pantalla Uno")
        }
    }
}
