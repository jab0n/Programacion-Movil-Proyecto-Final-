package com.example.programacion_movil_pruyecto_final

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.programacion_movil_pruyecto_final.data.AppDatabase
import com.example.programacion_movil_pruyecto_final.data.NotesRepository
import com.example.programacion_movil_pruyecto_final.data.TasksRepository
import com.example.programacion_movil_pruyecto_final.notifications.createNotificationChannel

class NotesAndTasksApplication : Application(), ImageLoaderFactory {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val notesRepository by lazy { NotesRepository(database.noteDao()) }
    val tasksRepository by lazy { TasksRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
