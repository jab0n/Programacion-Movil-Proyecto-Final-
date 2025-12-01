package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Embedded
import androidx.room.Relation

// Clase de datos que representa una nota con su lista de adjuntos.
// Se utiliza para obtener datos relacionados de diferentes tablas en una sola consulta.
data class NoteWithAttachments(
    // Anotación que indica que los campos de la entidad Note se pueden consultar directamente
    // como si estuvieran en esta clase.
    @Embedded val note: Note,
    // Anotación que define la relación entre la nota (Note) and sus adjuntos (Attachment).
    @Relation(
        parentColumn = "id", // La columna de la clave primaria en la entidad padre (Note).
        entityColumn = "noteId" // La columna de la clave foránea en la entidad hija (Attachment).
    )
    val attachments: List<Attachment>
)
