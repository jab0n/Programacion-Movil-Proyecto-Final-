package com.example.programacion_movil_pruyecto_final.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val date: String,
    val time: String
)
