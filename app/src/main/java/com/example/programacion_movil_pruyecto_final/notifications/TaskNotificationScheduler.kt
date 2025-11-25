package com.example.programacion_movil_pruyecto_final.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.data.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: Task) {
        if (task.date.isBlank() || task.time.isBlank()) {
            return
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val taskDateTime = try {
            formatter.parse("${task.date} ${task.time}")
        } catch (e: Exception) {
            return
        }

        if (taskDateTime == null) return

        val intervals = listOf(
            TimeUnit.HOURS.toMillis(24) to "24 hours",
            TimeUnit.HOURS.toMillis(1) to "1 hour",
            TimeUnit.MINUTES.toMillis(5) to "5 minutes",
            0L to "now"
        )

        val nowMillis = System.currentTimeMillis()

        for ((index, intervalPair) in intervals.withIndex()) {
            val (intervalMillis, intervalText) = intervalPair
            val notificationTimeMillis = taskDateTime.time - intervalMillis

            if (notificationTimeMillis > nowMillis) {
                val message = if (intervalMillis == 0L) {
                    "Your task \"${task.title}\" is due now."
                } else {
                    "Your task \"${task.title}\" is due in $intervalText."
                }

                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra(TaskNotificationWorker.KEY_TASK_ID, task.id)
                    putExtra(TaskNotificationWorker.KEY_TASK_TITLE, task.title)
                    putExtra(TaskNotificationWorker.KEY_MESSAGE, message)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id * 10 + index, 
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTimeMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    Toast.makeText(
                        context, 
                        context.getString(R.string.exact_alarm_permission_required),
                        Toast.LENGTH_LONG
                    ).show()

                    // Open app settings to allow the user to grant the permission.
                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
            }
        }
    }

    fun cancel(taskId: Int) {
        for (i in 0..3) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId * 10 + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
