package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.programacion_movil_pruyecto_final.R

class TaskNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

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

        return ForegroundInfo(1, notification) // ID must be > 0
    }

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val taskId = inputData.getInt(KEY_TASK_ID, 0)

        val notification = NotificationCompat.Builder(applicationContext, TASK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            notificationManager.notify(taskId, notification)
        } catch (e: SecurityException) {
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_MESSAGE = "message"
    }
}
