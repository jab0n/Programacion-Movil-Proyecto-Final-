package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Embedded
import androidx.room.Relation

// You may want to rename this file to TaskFull.kt to match the class name
data class TaskFull(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<Attachment>,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val reminders: List<Reminder>
)
