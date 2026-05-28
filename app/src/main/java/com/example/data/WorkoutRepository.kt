package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {
    // Calendario
    val calendarDays: Flow<List<CalendarDay>> = dao.getCalendarDays()
    suspend fun insertCalendarDay(day: CalendarDay) = dao.insertCalendarDay(day)
    suspend fun deleteCalendarDay(day: CalendarDay) = dao.deleteCalendarDay(day)

    // Rutinas
    val routines: Flow<List<Routine>> = dao.getRoutines()
    suspend fun insertRoutine(routine: Routine): Long = dao.insertRoutine(routine)
    suspend fun deleteRoutine(routine: Routine) = dao.deleteRoutine(routine)

    // Ejercicios
    fun getExercisesForRoutine(routineId: Long): Flow<List<Exercise>> = dao.getExercisesForRoutine(routineId)
    suspend fun insertExercise(exercise: Exercise): Long = dao.insertExercise(exercise)
    suspend fun deleteExercise(exercise: Exercise) = dao.deleteExercise(exercise)

    // Historial de Progreso
    val allProgressRecords: Flow<List<ProgressRecord>> = dao.getAllProgressRecords()
    fun getProgressForExercise(exerciseId: Long): Flow<List<ProgressRecord>> = dao.getProgressForExercise(exerciseId)
    suspend fun insertProgressRecord(record: ProgressRecord) = dao.insertProgressRecord(record)
    suspend fun deleteProgressRecord(record: ProgressRecord) = dao.deleteProgressRecord(record)

    // Biometría
    val biometricsFlow: Flow<Biometrics?> = dao.getBiometricsFlow()
    suspend fun getBiometrics(): Biometrics? = dao.getBiometrics()
    suspend fun insertBiometrics(biometrics: Biometrics) = dao.insertBiometrics(biometrics)

    // Historial IA
    val aiConversations: Flow<List<AiConversation>> = dao.getAiConversations()
    suspend fun insertAiConversation(conv: AiConversation) = dao.insertAiConversation(conv)
    suspend fun deleteAiConversation(conv: AiConversation) = dao.deleteAiConversation(conv)
    suspend fun clearAiConversations() = dao.clearAiConversations()

    // Fotos de Progreso
    val progressPhotos: Flow<List<ProgressPhoto>> = dao.getProgressPhotos()
    suspend fun insertProgressPhoto(photo: ProgressPhoto) = dao.insertProgressPhoto(photo)
    suspend fun deleteProgressPhoto(photo: ProgressPhoto) = dao.deleteProgressPhoto(photo)
    suspend fun clearProgressPhotos() = dao.clearProgressPhotos()

    // Clear All
    suspend fun clearAllData() {
        dao.clearCalendarDays()
        dao.clearRoutines()
        dao.clearExercises()
        dao.clearProgressRecords()
        dao.clearAiConversations()
        dao.clearProgressPhotos()
    }
}
