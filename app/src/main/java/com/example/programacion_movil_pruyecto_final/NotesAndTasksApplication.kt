package com.example.programacion_movil_pruyecto_final

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.programacion_movil_pruyecto_final.data.AppDatabase
import com.example.programacion_movil_pruyecto_final.data.NotesRepository
import com.example.programacion_movil_pruyecto_final.data.TasksRepository
import com.example.programacion_movil_pruyecto_final.notifications.createNotificationChannel

// Clase de la aplicación personalizada que se ejecuta al iniciar la aplicación.
class NotesAndTasksApplication : Application(), ImageLoaderFactory {
    // Inicialización perezosa de la base de datos y los repositorios.
    // `lazy` asegura que la base de datos y los repositorios se creen solo cuando se acceden por primera vez.
    private val database by lazy { AppDatabase.getDatabase(this) }
    val notesRepository by lazy { NotesRepository(database.noteDao()) }
    val tasksRepository by lazy { TasksRepository(database.taskDao()) }

    // Se llama cuando se crea la aplicación.
    override fun onCreate() {
        super.onCreate()
        // Crea el canal de notificaciones al iniciar la aplicación.
        createNotificationChannel(this)
    }

    // Crea y devuelve un nuevo ImageLoader para la biblioteca Coil.
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Añade un decodificador de fotogramas de video para poder mostrar miniaturas de videos.
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
