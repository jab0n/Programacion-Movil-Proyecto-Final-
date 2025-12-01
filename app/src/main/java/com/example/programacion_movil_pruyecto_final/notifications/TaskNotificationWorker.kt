package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.programacion_movil_pruyecto_final.R

// Un CoroutineWorker que se encarga de mostrar las notificaciones de las tareas en segundo plano.
class TaskNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Proporciona una notificación para que el worker se ejecute como un servicio en primer plano.
    // Es necesario para trabajos de larga duración en segundo plano.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title = applicationContext.getString(R.string.task_notification_channel_name)
        val message = "Checking for upcoming tasks..."

        val notification = NotificationCompat.Builder(applicationContext, TASK_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        return ForegroundInfo(1, notification) // El ID debe ser > 0
    }

    // El trabajo a realizar en segundo plano.
    override suspend fun doWork(): Result {
        // Extrae los datos de la tarea de los datos de entrada del worker.
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val taskId = inputData.getInt(KEY_TASK_ID, 0)

        // Construye la notificación.
        val notification = NotificationCompat.Builder(applicationContext, TASK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Obtiene el NotificationManager.
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            // Muestra la notificación.
            notificationManager.notify(taskId, notification)
        } catch (e: SecurityException) {
            // Si no se tienen los permisos para mostrar notificaciones, el trabajo falla.
            return Result.failure()
        }

        // Indica que el trabajo se ha completado con éxito.
        return Result.success()
    }

    // Constantes para las claves de los datos de entrada.
    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_MESSAGE = "message"
    }
}
