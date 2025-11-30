package com.example.programacion_movil_pruyecto_final

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.programacion_movil_pruyecto_final.notifications.NotificationReceiver
import com.example.programacion_movil_pruyecto_final.ui.screens.MediaViewerScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.NoteEntryScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.NotesScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.TaskEntryScreen
import com.example.programacion_movil_pruyecto_final.ui.screens.TasksScreen
import com.example.programacion_movil_pruyecto_final.ui.theme.ProgramacionMovilPruyectoFinalTheme

sealed class Screen(val route: String, val icon: ImageVector, val label: Int) {
    object Notes : Screen("notes", Icons.AutoMirrored.Filled.Note, R.string.notes)
    object Tasks : Screen("tasks", Icons.AutoMirrored.Filled.List, R.string.tasks)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as NotesAndTasksApplication

        val startDestination = if (intent?.action == NotificationReceiver.ACTION_SHOW_TASK_SCREEN) {
            Screen.Tasks.route
        } else {
            Screen.Notes.route
        }

        setContent {
            ProgramacionMovilPruyectoFinalTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val navController = rememberNavController()
                val items = listOf(Screen.Notes, Screen.Tasks)

                val showBottomBar = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
                val isExpandedScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomAppBar(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                            indicatorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(stringResource(screen.label)) },
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
                    }
                ) { innerPadding ->
                    Row(modifier = Modifier.padding(innerPadding)) {
                        if (!showBottomBar) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                items.forEach { screen ->
                                    NavigationRailItem(
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                            indicatorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(stringResource(screen.label)) },
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

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                        ) {
                            composable(Screen.Notes.route) {
                                NotesScreen(
                                    application = application,
                                    onAddNote = { navController.navigate("note_entry") },
                                    onEditNote = { noteId -> navController.navigate("note_entry/$noteId") },
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    },
                                    isExpandedScreen = isExpandedScreen
                                )
                            }
                            composable(Screen.Tasks.route) {
                                TasksScreen(
                                    application = application,
                                    onAddTask = { navController.navigate("task_entry") },
                                    onEditTask = { taskId -> navController.navigate("task_entry/$taskId") },
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    },
                                    isExpandedScreen = isExpandedScreen
                                )
                            }
                            composable("note_entry") {
                                NoteEntryScreen(
                                    application = application,
                                    onNavigateBack = { navController.popBackStack() },
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    }
                                )
                            }
                            composable(
                                route = "note_entry/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                NoteEntryScreen(
                                    application = application,
                                    onNavigateBack = { navController.popBackStack() },
                                    noteId = backStackEntry.arguments?.getInt("noteId"),
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    }
                                )
                            }
                            composable("task_entry") {
                                TaskEntryScreen(
                                    application = application,
                                    onNavigateBack = { navController.popBackStack() },
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    }
                                )
                            }
                            composable(
                                route = "task_entry/{taskId}",
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                TaskEntryScreen(
                                    application = application,
                                    onNavigateBack = { navController.popBackStack() },
                                    taskId = backStackEntry.arguments?.getInt("taskId"),
                                    onAttachmentClick = { uri, type ->
                                        val encodedUri = Uri.encode(uri)
                                        val encodedType = Uri.encode(type)
                                        navController.navigate("media_viewer/$encodedUri/$encodedType")
                                    }
                                )
                            }
                            composable(
                                "media_viewer/{uri}/{type}",
                                arguments = listOf(
                                    navArgument("uri") { type = NavType.StringType },
                                    navArgument("type") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val uri = backStackEntry.arguments?.getString("uri")!!
                                val type = backStackEntry.arguments?.getString("type")!!
                                MediaViewerScreen(
                                    uri = Uri.decode(uri),
                                    type = Uri.decode(type),
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
