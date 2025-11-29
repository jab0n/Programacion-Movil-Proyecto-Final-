package com.example.programacion_movil_pruyecto_final.utils

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionLauncher(
    onPermissionGranted: (action: String) -> Unit,
    onPermissionDenied: () -> Unit = {}
): (String, String) -> Unit {
    var actionToLaunch by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            actionToLaunch?.let(onPermissionGranted)
        } else {
            onPermissionDenied()
        }
        actionToLaunch = null
    }

    val context = LocalContext.current

    return { permission, action ->
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted(action)
        } else {
            // Here you could add a shouldShowRequestPermissionRationale check if needed
            actionToLaunch = action
            launcher.launch(permission)
        }
    }
}
