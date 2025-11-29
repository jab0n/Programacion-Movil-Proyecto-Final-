package com.example.programacion_movil_pruyecto_final.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.programacion_movil_pruyecto_final.data.AppDatabase
import com.example.programacion_movil_pruyecto_final.data.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Manually get dependencies as this is a background process
                    val database = AppDatabase.getDatabase(context)
                    val repository = TasksRepository(database.taskDao())
                    val scheduler = TaskNotificationScheduler(context)

                    // Get all tasks and reschedule their alarms
                    val tasks = repository.allTasks.first()
                    tasks.forEach { taskFull ->
                        if (!taskFull.task.isCompleted) {
                            scheduler.schedule(taskFull.task, taskFull.reminders)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
