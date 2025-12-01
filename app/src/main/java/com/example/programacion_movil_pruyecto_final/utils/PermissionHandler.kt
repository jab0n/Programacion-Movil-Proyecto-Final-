package com.example.programacion_movil_pruyecto_final.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Un Composable que gestiona la solicitud de permisos de una manera reutilizable y declarativa.
@Composable
fun rememberPermissionHandler(
    onGranted: (String) -> Unit // Landa que se ejecuta cuando el permiso es concedido.
): (String, String) -> Unit { // Devuelve una función que toma un permiso y una acción a realizar.
    val context = LocalContext.current
    var permissionToRequest by remember { mutableStateOf<String?>(null) }
    var actionToPerform by remember { mutableStateOf<String?>(null) }
    var showRationaleDialog by remember { mutableStateOf(false) } // Controla la visibilidad del diálogo de justificación.
    var showSettingsDialog by remember { mutableStateOf(false) } // Controla la visibilidad del diálogo para abrir los ajustes.

    // Lanza la solicitud de permiso.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        permissionToRequest?.let { permission ->
            if (isGranted) {
                // Si el permiso es concedido, ejecuta la acción.
                actionToPerform?.let(onGranted)
            } else {
                // Si el permiso es denegado, comprueba si se debe mostrar una justificación.
                if (!(context as Activity).shouldShowRequestPermissionRationale(permission)) {
                    showSettingsDialog = true
                }
            }
        }
        permissionToRequest = null
        actionToPerform = null
    }

    // Muestra un diálogo de justificación si es necesario.
    if (showRationaleDialog) {
        val permissionName = permissionToRequest?.substringAfterLast('.')?.lowercase() ?: "this"
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permission Required") },
            text = { Text("To use this feature, the $permissionName permission is required. Please grant it when prompted.") },
            confirmButton = { 
                Button(onClick = {
                    showRationaleDialog = false
                    permissionToRequest?.let { permissionLauncher.launch(it) }
                }) { Text("Continue") }
            },
            dismissButton = { Button(onClick = { showRationaleDialog = false }) { Text("Cancel") } }
        )
    }

    // Muestra un diálogo para abrir los ajustes si el permiso fue denegado permanentemente.
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission Permanently Denied") },
            text = { Text("You have permanently denied this permission. To use this feature, please enable it in the app settings.") },
            confirmButton = { 
                Button(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("Open Settings") }
            },
            dismissButton = { Button(onClick = { showSettingsDialog = false }) { Text("Cancel") } }
        )
    }

    // Devuelve una función que se puede llamar para solicitar un permiso.
    return { permission, action ->
        when {
            // Si el permiso ya está concedido, ejecuta la acción.
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                onGranted(action)
            }
            // Si se debe mostrar una justificación, muestra el diálogo correspondiente.
            (context as Activity).shouldShowRequestPermissionRationale(permission) -> {
                permissionToRequest = permission
                actionToPerform = action
                showRationaleDialog = true
            }
            // Si no, solicita el permiso.
            else -> {
                permissionToRequest = permission
                actionToPerform = action
                permissionLauncher.launch(permission)
            }
        }
    }
}
