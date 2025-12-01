package com.example.programacion_movil_pruyecto_final.utils

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Un Composable que gestiona la solicitud de permisos de una manera más simple y directa.
@Composable
fun rememberPermissionLauncher(
    onPermissionGranted: (action: String) -> Unit, // Landa que se ejecuta cuando el permiso es concedido.
    onPermissionDenied: () -> Unit = {} // Landa que se ejecuta cuando el permiso es denegado.
): (String, String) -> Unit { // Devuelve una función que toma un permiso y una acción a realizar.
    var actionToLaunch by remember { mutableStateOf<String?>(null) }

    // Lanza la solicitud de permiso.
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el permiso es concedido, ejecuta la acción.
            actionToLaunch?.let(onPermissionGranted)
        } else {
            // Si el permiso es denegado, ejecuta la acción correspondiente.
            onPermissionDenied()
        }
        actionToLaunch = null
    }

    val context = LocalContext.current

    // Devuelve una función que se puede llamar para solicitar un permiso.
    return { permission, action ->
        // Si el permiso ya está concedido, ejecuta la acción.
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted(action)
        } else {
            // Aquí se podría añadir una comprobación de shouldShowRequestPermissionRationale si fuera necesario.
            actionToLaunch = action
            launcher.launch(permission)
        }
    }
}
