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

// Clase que se encarga de programar y cancelar las notificaciones de las tareas.
class TaskNotificationScheduler(private val context: Context) {

    // Obtiene el servicio AlarmManager del sistema.
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Programa las notificaciones para una tarea y sus recordatorios.
    fun schedule(task: Task, reminders: List<Reminder>) {
        // Comprueba si la aplicación tiene permiso para programar alarmas exactas.
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                context,
                context.getString(R.string.exact_alarm_permission_required),
                Toast.LENGTH_LONG
            ).show()
            // Si no tiene permiso, abre la configuración para que el usuario lo conceda.
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        // Formateador de fecha y hora.
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // Itera sobre cada recordatorio para programar sus notificaciones.
        reminders.forEachIndexed { reminderIndex, reminder ->
            val taskDateTime = try {
                formatter.parse("${reminder.date} ${reminder.time}")
            } catch (e: Exception) {
                return@forEachIndexed // Salta este recordatorio si el formato no es válido.
            }

            if (taskDateTime == null) return@forEachIndexed

            // Define los intervalos de tiempo para las notificaciones.
            val intervals = listOf(
                TimeUnit.HOURS.toMillis(24) to "24 hours",
                TimeUnit.HOURS.toMillis(1) to "1 hour",
                TimeUnit.MINUTES.toMillis(5) to "5 minutes",
                0L to "now"
            )

            val nowMillis = System.currentTimeMillis()

            // Itera sobre los intervalos para programar una notificación para cada uno.
            intervals.forEachIndexed { intervalIndex, (intervalMillis, intervalText) ->
                val notificationTimeMillis = taskDateTime.time - intervalMillis

                // Solo programa la notificación si es en el futuro.
                if (notificationTimeMillis > nowMillis) {
                    val message = if (intervalMillis == 0L) {
                        "Your task \"${task.title}\" is due now."
                    } else {
                        "Your task \"${task.title}\" is due in $intervalText."
                    }

                    // Genera un código de solicitud único para el PendingIntent.
                    val requestCode = generateRequestCode(task.id, reminderIndex, intervalIndex)
                    // Crea el Intent para el BroadcastReceiver.
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra(TaskNotificationWorker.KEY_TASK_ID, task.id)
                        putExtra(TaskNotificationWorker.KEY_TASK_TITLE, task.title)
                        putExtra(TaskNotificationWorker.KEY_MESSAGE, message)
                    }

                    // Crea el PendingIntent que se activará en el momento de la notificación.
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        // Programa la alarma exacta.
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    } catch (e: SecurityException) {
                        // Este caso ya se maneja con la comprobación de canScheduleExactAlarms().
                    }
                }
            }
        }
    }

    // Cancela todas las notificaciones programadas para una tarea.
    fun cancel(task: Task, reminders: List<Reminder>) {
        reminders.forEachIndexed { reminderIndex, _ ->
            (0..3).forEach { intervalIndex ->
                val requestCode = generateRequestCode(task.id, reminderIndex, intervalIndex)
                val intent = Intent(context, NotificationReceiver::class.java)
                // Obtiene el PendingIntent sin crearlo si no existe.
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                // Si el PendingIntent existe, cancela la alarma.
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                }
            }
        }
    }
    
    // Genera un código de solicitud único para cada PendingIntent.
    private fun generateRequestCode(taskId: Int, reminderIndex: Int, intervalIndex: Int): Int {
        return (taskId * 1000) + (reminderIndex * 10) + intervalIndex
    }
}
