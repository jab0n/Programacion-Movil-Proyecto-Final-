package com.example.programacion_movil_pruyecto_final.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.programacion_movil_pruyecto_final.data.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskNotificationScheduler(private val context: Context) {

    fun schedule(task: Task) {
        if (task.date.isBlank() || task.time.isBlank()) {
            return
        }

        val workManager = WorkManager.getInstance(context)
        val tag = "task_notification_${task.id}"
        workManager.cancelAllWorkByTag(tag)

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

        for ((intervalMillis, intervalText) in intervals) {
            val notificationTimeMillis = taskDateTime.time - intervalMillis

            if (notificationTimeMillis > nowMillis) {
                val delay = notificationTimeMillis - nowMillis

                val message = if (intervalMillis == 0L) {
                    "Your task \"${task.title}\" is due now."
                } else {
                    "Your task \"${task.title}\" is due in $intervalText."
                }

                val data = Data.Builder()
                    .putInt(TaskNotificationWorker.KEY_TASK_ID, task.id)
                    .putString(TaskNotificationWorker.KEY_TASK_TITLE, task.title)
                    .putString(TaskNotificationWorker.KEY_MESSAGE, message)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag(tag)
                    .build()

                workManager.enqueue(workRequest)
            }
        }
    }

    fun cancel(taskId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_notification_$taskId")
    }
}
