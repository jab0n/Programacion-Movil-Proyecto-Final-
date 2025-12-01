package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Anotación que marca la clase como una entidad de Room. 
// Esto significa que será una tabla en la base de datos.
@Entity(
    tableName = "reminders",
    // Define una clave foránea para establecer una relación con la tabla de tareas (Task).
    foreignKeys = [ForeignKey(
        entity = Task::class, // La entidad padre de la relación.
        parentColumns = ["id"], // La columna de la clave primaria en la entidad padre.
        childColumns = ["taskId"], // La columna de la clave foránea en esta entidad.
        onDelete = ForeignKey.CASCADE // Cuando se elimina una tarea, también se eliminarán sus recordatorios asociados.
    )]
)
data class Reminder(
    // Anotación que marca el campo como la clave primaria de la tabla.
    // autoGenerate = true indica que Room debe generar automáticamente un valor para este campo.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int, // Clave foránea que referencia al ID de la tarea.
    val date: String, // Fecha del recordatorio.
    val time: String // Hora del recordatorio.
)
