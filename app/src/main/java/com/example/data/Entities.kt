package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_days")
data class CalendarDay(
    @PrimaryKey val date: String, // Formato "YYYY-MM-DD"
    val didTrain: Boolean,
    val note: String = ""
)

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val illustrationUrl: String = ""
)

@Entity(tableName = "progress_records")
data class ProgressRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val date: String, // Formato "YYYY-MM-DD"
    val weightKg: Double,
    val reps: Int
)

@Entity(tableName = "biometrics")
data class Biometrics(
    @PrimaryKey val id: Int = 1, // Fila única para biometría
    val weightKg: Double,
    val heightM: Double,
    val age: Int,
    val isMale: Boolean = true,
    val activityFactor: Double = 1.2, // Mifflin-St Jeor factor (1.2 = Sedentario, 1.375 = Ligero, etc)
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_conversations")
data class AiConversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Formato "YYYY-MM-DD"
    val imagePath: String, // Ruta del archivo local
    val note: String = ""
)
