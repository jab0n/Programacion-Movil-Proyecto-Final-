package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.programacion_movil_pruyecto_final.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra(TaskNotificationWorker.KEY_TASK_TITLE) ?: return
        val message = intent.getStringExtra(TaskNotificationWorker.KEY_MESSAGE) ?: return
        val taskId = intent.getIntExtra(TaskNotificationWorker.KEY_TASK_ID, 0)

        val notification = NotificationCompat.Builder(context, TASK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with a proper app icon
            .setContentTitle(taskTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, notification)
    }
}
