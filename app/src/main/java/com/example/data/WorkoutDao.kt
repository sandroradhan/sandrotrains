package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // Calendario
    @Query("SELECT * FROM calendar_days ORDER BY date DESC")
    fun getCalendarDays(): Flow<List<CalendarDay>>

    @Query("SELECT * FROM calendar_days WHERE date = :date LIMIT 1")
    suspend fun getCalendarDayByDate(date: String): CalendarDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarDay(day: CalendarDay)

    @Delete
    suspend fun deleteCalendarDay(day: CalendarDay)

    // Rutinas
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    // Ejercicios
    @Query("SELECT * FROM exercises WHERE routineId = :routineId ORDER BY id ASC")
    fun getExercisesForRoutine(routineId: Long): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // Historial de Progreso
    @Query("SELECT * FROM progress_records WHERE exerciseId = :exerciseId ORDER BY date ASC")
    fun getProgressForExercise(exerciseId: Long): Flow<List<ProgressRecord>>

    @Query("SELECT * FROM progress_records ORDER BY date DESC")
    fun getAllProgressRecords(): Flow<List<ProgressRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressRecord(record: ProgressRecord)

    @Delete
    suspend fun deleteProgressRecord(record: ProgressRecord)

    // Biometría
    @Query("SELECT * FROM biometrics WHERE id = 1 LIMIT 1")
    fun getBiometricsFlow(): Flow<Biometrics?>

    @Query("SELECT * FROM biometrics WHERE id = 1 LIMIT 1")
    suspend fun getBiometrics(): Biometrics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometrics(biometrics: Biometrics)

    // Historial IA
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC")
    fun getAiConversations(): Flow<List<AiConversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiConversation(conv: AiConversation): Long

    @Delete
    suspend fun deleteAiConversation(conv: AiConversation)

    @Query("DELETE FROM ai_conversations")
    suspend fun clearAiConversations()

    // Fotos de Progreso
    @Query("SELECT * FROM progress_photos ORDER BY date DESC, id DESC")
    fun getProgressPhotos(): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhoto): Long

    @Delete
    suspend fun deleteProgressPhoto(photo: ProgressPhoto)

    @Query("DELETE FROM progress_photos")
    suspend fun clearProgressPhotos()

    // Clear Queries
    @Query("DELETE FROM calendar_days")
    suspend fun clearCalendarDays()

    @Query("DELETE FROM routines")
    suspend fun clearRoutines()

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("DELETE FROM progress_records")
    suspend fun clearProgressRecords()
}
