package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // Lista de todos los registros de calendario
    val calendarDays: StateFlow<List<CalendarDay>> = repository.calendarDays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista de todas las rutinas registradas
    val routines: StateFlow<List<Routine>> = repository.routines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista de todos los registros de progreso
    val allProgressRecords: StateFlow<List<ProgressRecord>> = repository.allProgressRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Biometría actual
    val biometrics: StateFlow<Biometrics?> = repository.biometricsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Historial IA
    val aiConversations: StateFlow<List<AiConversation>> = repository.aiConversations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAiConversation(question: String, answer: String) {
        viewModelScope.launch {
            repository.insertAiConversation(
                AiConversation(question = question, answer = answer)
            )
        }
    }

    fun deleteAiConversation(conv: AiConversation) {
        viewModelScope.launch {
            repository.deleteAiConversation(conv)
        }
    }

    fun clearAiConversations() {
        viewModelScope.launch {
            repository.clearAiConversations()
        }
    }

    // Fotos de Progreso
    val progressPhotos: StateFlow<List<ProgressPhoto>> = repository.progressPhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProgressPhoto(date: String, imagePath: String, note: String) {
        viewModelScope.launch {
            repository.insertProgressPhoto(
                ProgressPhoto(date = date, imagePath = imagePath, note = note)
            )
        }
    }

    fun deleteProgressPhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            repository.deleteProgressPhoto(photo)
        }
    }

    // Estado del día seleccionado para inspección o log
    private val _selectedDateString = MutableStateFlow(getFormattedToday())
    val selectedDateString: StateFlow<String> = _selectedDateString.asStateFlow()

    // Rutina seleccionada para ver sus ejercicios
    private val _selectedRoutineId = MutableStateFlow<Long?>(null)
    val selectedRoutineId: StateFlow<Long?> = _selectedRoutineId.asStateFlow()

    // Ejercicios de la rutina seleccionada
    val selectedRoutineExercises: StateFlow<List<Exercise>> = _selectedRoutineId
        .flatMapLatest { routineId ->
            if (routineId != null) {
                repository.getExercisesForRoutine(routineId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            // Verificar si hay biometría registrada, si no, inicializar
            val currentBiometrics = repository.getBiometrics()
            if (currentBiometrics == null) {
                repository.insertBiometrics(
                    Biometrics(
                        weightKg = 75.0,
                        heightM = 1.78,
                        age = 26,
                        isMale = true,
                        activityFactor = 1.375
                    )
                )
            }

            // Seleccionar rutina predeterminada si existe
            val routinesList = repository.routines.first()
            if (routinesList.isNotEmpty()) {
                _selectedRoutineId.value = routinesList.first().id
            }
        }
    }

    // Limpieza de todos los datos de usuario
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedRoutineId.value = null
        }
    }

    // Funciones básicas de acción

    fun selectDate(dateString: String) {
        _selectedDateString.value = dateString
    }

    fun selectRoutine(routineId: Long) {
        _selectedRoutineId.value = routineId
    }

    fun toggleCalendarDay(date: String, didTrain: Boolean, note: String) {
        viewModelScope.launch {
            repository.insertCalendarDay(
                CalendarDay(date = date, didTrain = didTrain, note = note)
            )
        }
    }

    // Rutinas
    fun addRoutine(name: String, description: String) {
        viewModelScope.launch {
            val routineId = repository.insertRoutine(
                Routine(name = name, description = description)
            )
            _selectedRoutineId.value = routineId
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.insertRoutine(routine)
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            // Re-asignar si eliminamos la actual
            if (_selectedRoutineId.value == routine.id) {
                val currentList = repository.routines.first()
                val nextActive = currentList.firstOrNull { it.id != routine.id }
                _selectedRoutineId.value = nextActive?.id
            }
        }
    }

    // Ejercicios
    fun addExercise(name: String, sets: Int, reps: Int, restSeconds: Int, url: String) {
        val activeRoutineId = _selectedRoutineId.value ?: return
        viewModelScope.launch {
            repository.insertExercise(
                Exercise(
                    routineId = activeRoutineId,
                    name = name,
                    sets = sets,
                    reps = reps,
                    restSeconds = restSeconds,
                    illustrationUrl = url.ifEmpty { "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=400&q=80" }
                )
            )
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.insertExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    // Registros de Progreso
    fun addProgressRecord(exerciseId: Long, weight: Double, reps: Int, date: String) {
        viewModelScope.launch {
            repository.insertProgressRecord(
                ProgressRecord(
                    exerciseId = exerciseId,
                    date = date,
                    weightKg = weight,
                    reps = reps
                )
            )
        }
    }

    fun deleteProgressRecord(record: ProgressRecord) {
        viewModelScope.launch {
            repository.deleteProgressRecord(record)
        }
    }

    // Biometría
    fun updateBiometrics(weight: Double, height: Double, age: Int, isMale: Boolean, activityFactor: Double) {
        viewModelScope.launch {
            repository.insertBiometrics(
                Biometrics(
                    weightKg = weight,
                    heightM = height,
                    age = age,
                    isMale = isMale,
                    activityFactor = activityFactor,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CÁLCULOS METABÓLICOS
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Calcula la Tasa Metabólica Basal (TMB) utilizando la fórmula de Mifflin-St Jeor:
     * Hombres: TMB = 10 * peso (kg) + 6.25 * altura (cm) - 5 * edad (años) + 5
     * Mujeres: TMB = 10 * peso (kg) + 6.25 * altura (cm) - 5 * edad (años) - 161
     */
    fun calculateBMR(bio: Biometrics): Double {
        val heightCm = bio.heightM * 100
        return if (bio.isMale) {
            (10.0 * bio.weightKg) + (6.25 * heightCm) - (5.0 * bio.age) + 5.0
        } else {
            (10.0 * bio.weightKg) + (6.25 * heightCm) - (5.0 * bio.age) - 161.0
        }
    }

    /**
     * Calcula el consumo calórico diario en base a la Tasa Metabólica Basal y
     * el factor de actividad física (ya sea el configurado o el guiado dinámicamente)
     */
    fun calculateDailyExpenditure(bio: Biometrics, dynamicFactor: Double): Double {
        val bmr = calculateBMR(bio)
        return bmr * dynamicFactor
    }

    /**
     * Calcula un factor de actividad dinámico basado en cuántos días entrenó
     * de los últimos 30 días cargados en el calendario de la aplicación.
     *
     * Regla:
     * - 0 entrenamientos (Sedentario): factor = 1.2
     * - 1 - 2 entrenamientos por semana (Promedio 4-10 al mes - Actividad Ligera): factor = 1.375
     * - 3 - 4 entrenamientos por semana (Promedio 11-18 al mes - Actividad Moderada): factor = 1.55
     * - 5+ entrenamientos por semana (Promedio 19+ al mes - Muy Activo): factor = 1.725
     */
    fun getDynamicActivityFactor(days: List<CalendarDay>): Double {
        val workoutDaysInLast30 = days.filter { it.didTrain }.size
        return when {
            workoutDaysInLast30 == 0 -> 1.2
            workoutDaysInLast30 <= 8 -> 1.375
            workoutDaysInLast30 <= 16 -> 1.55
            else -> 1.725
        }
    }

    companion object {
        fun getFormattedToday(): String {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return df.format(Date())
        }

        fun getFormattedDate(date: Date): String {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return df.format(date)
        }
    }
}

// Factoría para crear la instancia del ViewModel con inyección manual
class WorkoutViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
