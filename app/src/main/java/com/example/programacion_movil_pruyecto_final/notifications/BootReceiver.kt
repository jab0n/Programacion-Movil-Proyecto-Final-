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

// Un BroadcastReceiver que se activa cuando el dispositivo termina de arrancar.
class BootReceiver : BroadcastReceiver() {

    // Este método se llama cuando el BroadcastReceiver recibe un Intent.
    override fun onReceive(context: Context, intent: Intent) {
        // Comprueba si la acción del Intent es la de arranque completado.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            // Lanza una corutina en el hilo de IO para realizar operaciones de larga duración.
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Obtiene manualmente las dependencias, ya que se trata de un proceso en segundo plano.
                    val database = AppDatabase.getDatabase(context)
                    val repository = TasksRepository(database.taskDao())
                    val scheduler = TaskNotificationScheduler(context)

                    // Obtiene todas las tareas y reprograma sus alarmas.
                    val tasks = repository.allTasks.first()
                    tasks.forEach { taskFull ->
                        if (!taskFull.task.isCompleted) {
                            scheduler.schedule(taskFull.task, taskFull.reminders)
                        }
                    }
                } finally {
                    // Finaliza el resultado pendiente para indicar que el trabajo ha terminado.
                    pendingResult.finish()
                }
            }
        }
    }
}
