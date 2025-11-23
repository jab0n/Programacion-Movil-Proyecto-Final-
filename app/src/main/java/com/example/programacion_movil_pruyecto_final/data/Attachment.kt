package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int?,
    val taskId: Int?,
    val uri: String,
    val type: String
)
