package com.example.programacion_movil_pruyecto_final

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.programacion_movil_pruyecto_final.ui.screens.AddNoteScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.AddTaskScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.EditNoteScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.NotesScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.TasksScreen

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Notes : Screen("notes", Icons.Default.Note, "Notas")
    object Tasks : Screen("tasks", Icons.Default.List, "Tareas")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as NotesAndTasksApplication
        setContent {
            val navController = rememberNavController()
            val items = listOf(Screen.Notes, Screen.Tasks)

            Scaffold(
                bottomBar = {
                    BottomAppBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Notes.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Notes.route) { 
                        NotesScreen(
                            application,
                            onAddNote = { navController.navigate("add_note") },
                            onEditNote = { noteId -> navController.navigate("edit_note/$noteId") }
                        )
                    }
                    composable(Screen.Tasks.route) { TasksScreen(application, onAddTask = { navController.navigate("add_task") }) }
                    composable("add_note") { AddNoteScreen(application) { navController.popBackStack() } }
                    composable("add_task") { AddTaskScreen(application) { navController.popBackStack() } }
                    composable(
                        route = "edit_note/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) {
                        EditNoteScreen(
                            application = application,
                            noteId = it.arguments?.getInt("noteId") ?: 0,
                            onNoteUpdated = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}