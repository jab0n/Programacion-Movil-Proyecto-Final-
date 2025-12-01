package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Embedded
import androidx.room.Relation

// Clase de datos que representa una tarea con su lista de adjuntos y recordatorios.
// Se utiliza para obtener datos relacionados de diferentes tablas en una sola consulta.
// Es recomendable que el nombre del archivo coincida con el nombre de la clase (TaskFull.kt).
data class TaskFull(
    // Anotación que indica que los campos de la entidad Task se pueden consultar directamente
    // como si estuvieran en esta clase.
    @Embedded val task: Task,
    // Anotación que define la relación entre la tarea (Task) y sus adjuntos (Attachment).
    @Relation(
        parentColumn = "id", // La columna de la clave primaria en la entidad padre (Task).
        entityColumn = "taskId" // La columna de la clave foránea en la entidad hija (Attachment).
    )
    val attachments: List<Attachment>,
    // Anotación que define la relación entre la tarea (Task) y sus recordatorios (Reminder).
    @Relation(
        parentColumn = "id", // La columna de la clave primaria en la entidad padre (Task).
        entityColumn = "taskId" // La columna de la clave foránea en la entidad hija (Reminder).
    )
    val reminders: List<Reminder>
)
