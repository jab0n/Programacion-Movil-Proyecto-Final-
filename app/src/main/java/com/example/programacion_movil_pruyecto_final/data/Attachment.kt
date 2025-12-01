package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Anotación que marca la clase como una entidad de Room. 
// Esto significa que será una tabla en la base de datos.
@Entity(
    tableName = "attachments",
    // Define las claves foráneas para establecer relaciones con las tablas de notas y tareas.
    foreignKeys = [
        ForeignKey(
            entity = Note::class, // La entidad padre (Note).
            parentColumns = ["id"], // La clave primaria de la entidad padre.
            childColumns = ["noteId"], // La clave foránea en esta entidad.
            onDelete = ForeignKey.CASCADE // Si se elimina una nota, también se eliminarán sus adjuntos.
        ),
        ForeignKey(
            entity = Task::class, // La entidad padre (Task).
            parentColumns = ["id"], // La clave primaria de la entidad padre.
            childColumns = ["taskId"], // La clave foránea en esta entidad.
            onDelete = ForeignKey.CASCADE // Si se elimina una tarea, también se eliminarán sus adjuntos.
        )
    ]
)
data class Attachment(
    // Anotación que marca el campo como la clave primaria de la tabla.
    // autoGenerate = true indica que Room debe generar automáticamente un valor para este campo.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Clave foránea que referencia al ID de la nota (puede ser nulo).
    val noteId: Int?,
    // Clave foránea que referencia al ID de la tarea (puede ser nulo).
    val taskId: Int?,
    // La URI que representa la ubicación del archivo adjunto.
    val uri: String,
    // El tipo MIME del archivo adjunto (ej. "image/jpeg", "video/mp4").
    val type: String
)
