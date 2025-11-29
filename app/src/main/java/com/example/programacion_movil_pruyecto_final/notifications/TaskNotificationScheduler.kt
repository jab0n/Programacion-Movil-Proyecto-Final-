package com.example.programacion_movil_pruyecto_final.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.data.Reminder
import com.example.programacion_movil_pruyecto_final.data.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: Task, reminders: List<Reminder>) {
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                context,
                context.getString(R.string.exact_alarm_permission_required),
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        reminders.forEachIndexed { reminderIndex, reminder ->
            val taskDateTime = try {
                formatter.parse("${reminder.date} ${reminder.time}")
            } catch (e: Exception) {
                return@forEachIndexed // Skip this reminder if format is invalid
            }

            if (taskDateTime == null) return@forEachIndexed

            val intervals = listOf(
                TimeUnit.HOURS.toMillis(24) to "24 hours",
                TimeUnit.HOURS.toMillis(1) to "1 hour",
                TimeUnit.MINUTES.toMillis(5) to "5 minutes",
                0L to "now"
            )

            val nowMillis = System.currentTimeMillis()

            intervals.forEachIndexed { intervalIndex, (intervalMillis, intervalText) ->
                val notificationTimeMillis = taskDateTime.time - intervalMillis

                if (notificationTimeMillis > nowMillis) {
                    val message = if (intervalMillis == 0L) {
                        "Your task \"${task.title}\" is due now."
                    } else {
                        "Your task \"${task.title}\" is due in $intervalText."
                    }

                    val requestCode = generateRequestCode(task.id, reminderIndex, intervalIndex)
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra(TaskNotificationWorker.KEY_TASK_ID, task.id)
                        putExtra(TaskNotificationWorker.KEY_TASK_TITLE, task.title)
                        putExtra(TaskNotificationWorker.KEY_MESSAGE, message)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
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
                        // This case is now handled by the canScheduleExactAlarms() check above
                    }
                }
            }
        }
    }

    fun cancel(task: Task, reminders: List<Reminder>) {
        reminders.forEachIndexed { reminderIndex, _ ->
            (0..3).forEach { intervalIndex ->
                val requestCode = generateRequestCode(task.id, reminderIndex, intervalIndex)
                val intent = Intent(context, NotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                }
            }
        }
    }
    
    private fun generateRequestCode(taskId: Int, reminderIndex: Int, intervalIndex: Int): Int {
        return (taskId * 1000) + (reminderIndex * 10) + intervalIndex
    }
}
