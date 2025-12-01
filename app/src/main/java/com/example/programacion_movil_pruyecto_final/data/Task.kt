package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Anotación que marca la clase como una entidad de Room. 
// Esto significa que será una tabla en la base de datos.
@Entity(tableName = "tasks")
data class Task(
    // Anotación que marca el campo como la clave primaria de la tabla.
    // autoGenerate = true indica que Room debe generar automáticamente un valor para este campo.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val isCompleted: Boolean = false
)
