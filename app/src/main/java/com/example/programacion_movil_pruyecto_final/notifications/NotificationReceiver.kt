package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.programacion_movil_pruyecto_final.MainActivity
import com.example.programacion_movil_pruyecto_final.R

// Un BroadcastReceiver que se encarga de recibir y mostrar las notificaciones de las tareas.
class NotificationReceiver : BroadcastReceiver() {

    // Este método se llama cuando el BroadcastReceiver recibe un Intent.
    override fun onReceive(context: Context, intent: Intent) {
        // Extrae los datos de la tarea del Intent.
        val taskTitle = intent.getStringExtra(TaskNotificationWorker.KEY_TASK_TITLE) ?: return
        val message = intent.getStringExtra(TaskNotificationWorker.KEY_MESSAGE) ?: return
        val taskId = intent.getIntExtra(TaskNotificationWorker.KEY_TASK_ID, 0)

        // Crea un Intent para abrir la MainActivity cuando se toque la notificación.
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SHOW_TASK_SCREEN // Acción personalizada para ir a la pantalla de tareas.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Crea un PendingIntent que se activará cuando el usuario toque la notificación.
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId, // Usa un código de solicitud único para cada notificación.
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construye la notificación utilizando NotificationCompat.Builder.
        val notification = NotificationCompat.Builder(context, TASK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Reemplazar con un icono de la aplicación adecuado.
            .setContentTitle(taskTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // La notificación se elimina al tocarla.
            .setContentIntent(pendingIntent) // Establece la acción a realizar al hacer clic.
            .build()

        // Obtiene el NotificationManager y muestra la notificación.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, notification)
    }

    companion object {
        // Constante para la acción del Intent que abre la pantalla de tareas.
        const val ACTION_SHOW_TASK_SCREEN = "ACTION_SHOW_TASK_SCREEN"
    }
}
