package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithAttachments(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<Attachment>
)
