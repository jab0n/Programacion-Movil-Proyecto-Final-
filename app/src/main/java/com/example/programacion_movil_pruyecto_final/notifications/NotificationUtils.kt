package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.programacion_movil_pruyecto_final.R

// ID constante para el canal de notificaciones de tareas.
const val TASK_NOTIFICATION_CHANNEL_ID = "task_notifications"

// Función para crear el canal de notificaciones para las tareas.
// Es necesario en Android 8.0 (API 26) y versiones posteriores.
fun createNotificationChannel(context: Context) {
    // La creación de canales solo es necesaria en Android O (Oreo) o superior.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Nombre del canal visible para el usuario.
        val name = context.getString(R.string.task_notification_channel_name)
        // Descripción del canal visible para el usuario.
        val descriptionText = context.getString(R.string.task_notification_channel_description)
        // Importancia de las notificaciones en este canal.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // Creación del objeto NotificationChannel.
        val channel = NotificationChannel(TASK_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Obtiene el servicio NotificationManager del sistema.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Registra el canal en el sistema.
        notificationManager.createNotificationChannel(channel)
    }
}
