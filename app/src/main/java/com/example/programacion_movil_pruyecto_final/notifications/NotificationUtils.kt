package com.example.programacion_movil_pruyecto_final.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.programacion_movil_pruyecto_final.R

const val TASK_NOTIFICATION_CHANNEL_ID = "task_notifications"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.task_notification_channel_name)
        val descriptionText = context.getString(R.string.task_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(TASK_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
