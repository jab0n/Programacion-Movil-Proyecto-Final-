package com.example.programacion_movil_pruyecto_final.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

// Composable que representa la pantalla para visualizar archivos multimedia (imágenes, videos, audio).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(uri: String, type: String?, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val mediaUri = Uri.parse(uri)

    // Estructura de la pantalla.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Si el tipo de medio es una imagen, muestra la imagen.
            if (type?.startsWith("image/") == true) {
                AsyncImage(
                    model = mediaUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            // Si el tipo de medio es un video o audio, muestra el reproductor de medios.
            } else if (type?.startsWith("video/") == true || type?.startsWith("audio/") == true) {
                // Recuerda una instancia de ExoPlayer.
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(mediaUri))
                        prepare()
                        playWhenReady = true
                    }
                }

                // Utiliza AndroidView para incrustar la vista del reproductor de ExoPlayer.
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Efecto que se ejecuta al salir de la pantalla para liberar los recursos de ExoPlayer.
                DisposableEffect(Unit) {
                    onDispose { exoPlayer.release() }
                }
            }
        }
    }
}
