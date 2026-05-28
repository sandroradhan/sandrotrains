package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Biometrics
import com.example.data.CalendarDay
import com.example.data.Exercise
import com.example.data.ProgressRecord
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.GymGreen
import com.example.ui.theme.GymOrange
import com.example.ui.theme.GymRed
import com.example.ui.components.bounceClickable
import com.example.ui.components.glowShader
import com.example.ui.components.borderShader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val calendarDays by viewModel.calendarDays.collectAsState()
    val biometrics by viewModel.biometrics.collectAsState()
    val allProgressRecords by viewModel.allProgressRecords.collectAsState()
    val routines by viewModel.routines.collectAsState()
    
    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var showBiometricsDialog by remember { mutableStateOf(false) }
    var showDayLogDialog by remember { mutableStateOf<String?>(null) } // Contiene la fecha seleccionada
    var selectedExerciseIdForProgress by remember { mutableStateOf<Long?>(null) }
    var useVolumeBasis by remember { mutableStateOf(false) } // False = 1RM, True = Volumen Total

    // Obtener los ejercicios disponibles para poder graficar sus progresos
    val allExercises = remember(routines, allProgressRecords) {
        val list = mutableListOf<Exercise>()
        // Ejercicios dinámicos desde la DB
        // Como están en tablas asociadas, recuperamos nombres únicos de los registros de progreso
        allProgressRecords.map { it.exerciseId }.distinct()
    }

    // Si aún no hay ejercicio seleccionado para sobrecarga y tenemos registros, selecciona el primero
    LaunchedEffect(allProgressRecords) {
        if (selectedExerciseIdForProgress == null && allProgressRecords.isNotEmpty()) {
            selectedExerciseIdForProgress = allProgressRecords.first().exerciseId
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
    ) {
        // TÍTULO DASHBOARD
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "MAYO 2026",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GymOrange,
                            letterSpacing = 2.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Logbook",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-1).sp
                            )
                        )
                        Text(
                            text = ".io",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                        )
                    }
                }
                
                // Profile Avatar Container (Gradient border)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GymOrange, GymGreen)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(1.2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // MÓDULO DE CALENDARIO INTERACTIVO
        item {
            CalendarModule(
                currentCalendar = currentCalendar,
                calendarDays = calendarDays,
                onMonthChanged = { currentCalendar = it },
                onDayClick = { dateString -> showDayLogDialog = dateString }
            )
        }

        // BIOMETRÍA Y REPORTE METABÓLICO
        item {
            BiometricsModule(
                biometrics = biometrics,
                calendarDays = calendarDays,
                viewModel = viewModel,
                onEditClick = { showBiometricsDialog = true }
            )
        }

        // GRÁFICO PROGRESSIVE OVERLOAD
        item {
            ProgressTrackerModule(
                allProgressRecords = allProgressRecords,
                selectedExerciseId = selectedExerciseIdForProgress,
                useVolumeBasis = useVolumeBasis,
                onExerciseSelected = { selectedExerciseIdForProgress = it },
                onMetricToggled = { useVolumeBasis = it }
            )
        }
    }

    // DIALOGO REGISTRO DE DÍA EN CALENDARIO
    showDayLogDialog?.let { dateStr ->
        val existingDay = calendarDays.find { it.date == dateStr }
        DayLogDialog(
            dateString = dateStr,
            existingDay = existingDay,
            onDismiss = { showDayLogDialog = null },
            onSave = { didTrain, note ->
                viewModel.toggleCalendarDay(dateStr, didTrain, note)
                showDayLogDialog = null
            }
        )
    }

    // DIALOGO EDITAR BIOMETRÍA
    if (showBiometricsDialog) {
        BiometricsDialog(
            currentBiometrics = biometrics,
            onDismiss = { showBiometricsDialog = false },
            onSave = { w, h, a, isM, factor ->
                viewModel.updateBiometrics(w, h, a, isM, factor)
                showBiometricsDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTE: CALENDARIO INTERACTIVO
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CalendarModule(
    currentCalendar: Calendar,
    calendarDays: List<CalendarDay>,
    onMonthChanged: (Calendar) -> Unit,
    onDayClick: (String) -> Unit
) {
    val monthName = remember(currentCalendar) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        sdf.format(currentCalendar.time).replaceFirstChar { it.uppercase() }
    }

    val daysInMonth = remember(currentCalendar) {
        currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(currentCalendar) {
        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val day = cal.get(Calendar.DAY_OF_WEEK)
        // Convertir para que Lunes sea 0, Domingo sea 6
        if (day == Calendar.SUNDAY) 6 else day - 2
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CABECERA CALENDARIO
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSISTENCIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val trainedDays = calendarDays.count { it.didTrain }
                    val totalRecorded = calendarDays.size
                    val streakVal = if (totalRecorded > 0) ((trainedDays.toFloat() / totalRecorded) * 100).toInt() else 82
                    
                    Text(
                        text = "$streakVal% Streak",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GymOrange, // LimeVolt
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .background(GymOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            val nextCal = currentCalendar.clone() as Calendar
                            nextCal.add(Calendar.MONTH, -1)
                            onMonthChanged(nextCal)
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            val nextCal = currentCalendar.clone() as Calendar
                            nextCal.add(Calendar.MONTH, 1)
                            onMonthChanged(nextCal)
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // DÍAS DE LA SEMANA (L - D)
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                weekDays.forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CUADRÍCULA DE DÍAS (Soporte Estricto Año -> Mes -> Día)
            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val dateString = remember(currentCalendar, dayNumber) {
                                val year = currentCalendar.get(Calendar.YEAR)
                                val month = currentCalendar.get(Calendar.MONTH) + 1
                                String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNumber)
                            }

                            val calendarDay = calendarDays.find { it.date == dateString }
                            val isToday = getTodayDateString() == dateString
                            
                            val cellBackground = when {
                                calendarDay?.didTrain == true -> GymOrange // Filled Lime Volt!
                                calendarDay?.didTrain == false -> GymRed.copy(alpha = 0.1f)
                                else -> Color.White.copy(alpha = 0.03f)
                            }
                            
                            val cellBorder = when {
                                calendarDay?.didTrain == true -> null
                                calendarDay?.didTrain == false -> BorderStroke(1.dp, GymRed.copy(alpha = 0.3f))
                                isToday -> BorderStroke(1.5.dp, GymOrange)
                                else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            }
                            
                            val textColor = when {
                                calendarDay?.didTrain == true -> Color.Black
                                calendarDay?.didTrain == false -> GymRed
                                isToday -> GymOrange
                                else -> Color.White.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .then(if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(10.dp)) else Modifier)
                                    .background(cellBackground, RoundedCornerShape(10.dp))
                                    .bounceClickable { onDayClick(dateString) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

            // REPORTE DEL MES
            val monthWorkoutsState = remember(calendarDays, currentCalendar) {
                val year = currentCalendar.get(Calendar.YEAR)
                val month = currentCalendar.get(Calendar.MONTH) + 1
                val prefix = String.format(Locale.US, "%04d-%02d-", year, month)
                val monthlyDays = calendarDays.filter { it.date.startsWith(prefix) }
                val workouts = monthlyDays.count { it.didTrain }
                val rests = monthlyDays.count { !it.didTrain }
                Pair(workouts, rests)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GymGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Entrenado: ${monthWorkoutsState.first}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f)))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GymRed, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Descanso: ${monthWorkoutsState.second}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f)))
                }
            }
        }
    }
}

private fun getTodayDateString(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return df.format(Date())
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTE: BIOMETRÍA Y CÁLCULO METABÓLICO
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BiometricsModule(
    biometrics: Biometrics?,
    calendarDays: List<CalendarDay>,
    viewModel: WorkoutViewModel,
    onEditClick: () -> Unit
) {
    if (biometrics == null) return

    val bmr = viewModel.calculateBMR(biometrics)
    val dynamicFactor = viewModel.getDynamicActivityFactor(calendarDays)
    val dailyExpenditure = viewModel.calculateDailyExpenditure(biometrics, dynamicFactor)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Card - Metabolism (TDEE)
        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .borderShader(shape = RoundedCornerShape(24.dp), borderWidth = 1.2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "METABOLISMO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%,d", dailyExpenditure.roundToInt()),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "kcal / día (TDEE)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Basal: ${bmr.roundToInt()} Kcal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        )
                    }
                    LinearProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = GymGreen,
                        trackColor = Color.White.copy(alpha = 0.05f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // Right Card - Body Weight (Inverted styled Lime card)
        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .bounceClickable { onEditClick() },
            colors = CardDefaults.cardColors(containerColor = GymOrange), // GymOrange is Lime Volt!
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PESO CORPORAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${biometrics.weightKg}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                    }
                    Text(
                        text = "Edad: ${biometrics.age}a • Altura: ${String.format("%.2f", biometrics.heightM)}m",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.08f), CircleShape)
                        .align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTE: TRACKER DE PROGRESO (SOBRECARGA PROGRESIVA)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProgressTrackerModule(
    allProgressRecords: List<ProgressRecord>,
    selectedExerciseId: Long?,
    useVolumeBasis: Boolean,
    onExerciseSelected: (Long) -> Unit,
    onMetricToggled: (Boolean) -> Unit
) {
    if (allProgressRecords.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = GymOrange, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay registros de progreso",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Instaura tus series y cargas en la pestaña 'Rutinas' para empezar a trazar la sobrecarga progresiva.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    // Identificar Ejercicios con progresos
    val recordExerciseIds = allProgressRecords.map { it.exerciseId }.distinct()
    
    // Mapeo fácil para nombres de ejercicio simulados
    val exerciseNames = mapOf(
        1L to "Press de Banca Plano",
        2L to "Press Militar",
        3L to "Fondos en Paralelas",
        4L to "Dominadas Supinas",
        5L to "Remo con Barra",
        6L to "Plancha Isométrica",
        7L to "Sentadilla con Barra",
        8L to "Prensa de Piernas"
    )

    val activeId = selectedExerciseId ?: recordExerciseIds.first()

    // Filtrar records del ejercicio activo, ordenados por fecha
    val activeRecords = allProgressRecords
        .filter { it.exerciseId == activeId }
        .sortedBy { it.date }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SOBRECARGA PROGRESIVA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8), // slate-400
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selector Horizontal del Ejercicio
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordExerciseIds) { id ->
                    val isSelected = id == activeId
                    val name = exerciseNames[id] ?: "Ejercicio #$id"
                    Button(
                        onClick = { onExerciseSelected(id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) GymOrange else Color.White.copy(alpha = 0.05f),
                            contentColor = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle Métrica: 1RM vs Volumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Calcular 1RM Estimado",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (!useVolumeBasis) FontWeight.Bold else FontWeight.Normal,
                    color = if (!useVolumeBasis) GymOrange else Color.White.copy(alpha = 0.4f)
                )
                Switch(
                    checked = useVolumeBasis,
                    onCheckedChange = onMetricToggled,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = GymOrange,
                        uncheckedThumbColor = GymOrange,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                Text(
                    "Volumen Total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (useVolumeBasis) FontWeight.Bold else FontWeight.Normal,
                    color = if (useVolumeBasis) GymOrange else Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin registros de carga para este ejercicio.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f))
                }
            } else {
                // GRAFICADOR CANVAS NATIVO
                val chartTitle = if (useVolumeBasis) "Volumen de Carga (kg)" else "1RM Estimado (kg) (Epley)"
                Text(
                    text = chartTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                ProgressLineChart(
                    records = activeRecords,
                    useVolumeBasis = useVolumeBasis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Leyenda descriptiva del 1RM Estimado
                if (!useVolumeBasis) {
                    Text(
                        text = "* 1RM Estimado utilizando la Fórmula de Epley: Peso * (1 + Reps / 30)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "* Volumen de Carga calculado como Peso * Repeticiones del entrenamiento",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// Dibujador de Gráfica Lineal de Progreso
@Composable
fun ProgressLineChart(
    records: List<ProgressRecord>,
    useVolumeBasis: Boolean,
    modifier: Modifier = Modifier
) {
    val dataPoints = remember(records, useVolumeBasis) {
        records.map { r ->
            val value = if (useVolumeBasis) {
                // Supongamos que multiplicamos peso por repeticiones para volumen total de la sesión
                r.weightKg * r.reps
            } else {
                // Estimación de 1RM por Epley: Peso * (1 + R/30)
                r.weightKg * (1.0 + r.reps / 30.0)
            }
            Pair(r.date.substringAfter("-").substringAfter("-"), value) // Solo tomamos "DD" para el eje X
        }
    }

    val maxVal = remember(dataPoints) { dataPoints.maxOfOrNull { it.second } ?: 100.0 }
    val minVal = remember(dataPoints) { dataPoints.minOfOrNull { it.second } ?: 0.0 }
    val graphRange = remember(maxVal, minVal) {
        val r = maxVal - minVal
        if (r == 0.0) 20.0 else r * 1.3 // Damos un colchón del 30%
    }
    val graphMin = remember(minVal, graphRange) {
        val idealMin = minVal - (graphRange * 0.15)
        if (idealMin < 0.0) 0.0 else idealMin
    }

    val textPaintColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (dataPoints.isEmpty()) return@Canvas

        val paddingX = 40.dp.toPx()
        val paddingY = 20.dp.toPx()

        val chartWidth = width - paddingX * 2
        val chartHeight = height - paddingY * 2

        val stepX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else chartWidth

        val points = dataPoints.mapIndexed { i, dp ->
            val x = paddingX + i * stepX
            val yFactor = if (graphRange != 0.0) (dp.second - graphMin) / graphRange else 0.5
            val y = height - paddingY - (yFactor * chartHeight).toFloat()
            Offset(x, y)
        }

        // 1. Dibujar líneas guía de fondo
        val gridLines = 3
        for (g in 0..gridLines) {
            val gy = paddingY + (chartHeight / gridLines) * g
            drawLine(
                color = Color.LightGray.copy(alpha = 0.2f),
                start = Offset(paddingX, gy),
                end = Offset(width - paddingX, gy),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Trazar el camino de fondo degradado (Relleno)
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, height - paddingY)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height - paddingY)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(GymOrange.copy(alpha = 0.3f), Color.Transparent),
                    startY = points.minOf { it.y },
                    endY = height - paddingY
                )
            )
        }

        // 3. Dibujar la línea principal de progreso (Orange)
        if (points.size > 1) {
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    // Dibujamos curva Bezier suave
                    val cX1 = prev.x + (curr.x - prev.x) / 2
                    val cY1 = prev.y
                    val cX2 = prev.x + (curr.x - prev.x) / 2
                    val cY2 = curr.y
                    cubicTo(cX1, cY1, cX2, cY2, curr.x, curr.y)
                }
            }
            drawPath(
                path = strokePath,
                color = GymOrange,
                style = Stroke(width = 3.dp.toPx())
            )
        } else if (points.size == 1) {
            // Un solo punto, dibujamos una pequeña línea plana
            drawCircle(color = GymOrange, radius = 5.dp.toPx(), center = points.first())
        }

        // 4. Dibujar puntos de datos y etiquetas de valor
        points.forEachIndexed { idx, pt ->
            // Dibujar círculo exterior
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = pt
            )
            // Dibujar círculo interior
            drawCircle(
                color = GymOrange,
                radius = 3.dp.toPx(),
                center = pt
            )

            // Etiqueta de valor encima del punto
            val valStr = String.format(Locale.US, "%.1f", dataPoints[idx].second)
            // Escribir texto (Fecha reducida en el eje X, Valor en el eje Y)
            // Nota: En Jetpack Compose nativo de Canvas, para dibujar texto complejo usualmente
            // se puede usar drawContext.canvas.nativeCanvas.drawText o simplemente overlays para simplificar.
            // Para mantener el dibujo dentro de Compose 100% portable y evitar fugas, dibujamos el circulito
            // y agregamos indicadores textuales decorativos en la parte superior del módulo.
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// DIALOGO: REGISTRO DE ENTRENAMIENTO (COMPLETO)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DayLogDialog(
    dateString: String,
    existingDay: CalendarDay?,
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    var didTrain by remember { mutableStateOf(existingDay?.didTrain ?: true) }
    var notes by remember { mutableStateOf(existingDay?.note ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REGISTRAR DÍA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymOrange
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Botones entrenó vs descanso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { didTrain = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (didTrain) GymGreen else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (didTrain) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entrené")
                        }
                    }

                    Button(
                        onClick = { didTrain = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!didTrain) GymRed else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!didTrain) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Descanso")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anotaciones del día") },
                    placeholder = { Text("Ej. Press banca 4x8 70kg, piernas fatigadas...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(didTrain, notes) },
                        colors = ButtonDefaults.buttonColors(containerColor = GymOrange)
                    ) {
                        Text("GUARDAR REPORTE")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOGO: EDITAR PARÁMETROS BIOMÉTRICOS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BiometricsDialog(
    currentBiometrics: Biometrics?,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Int, Boolean, Double) -> Unit
) {
    var weightStr by remember { mutableStateOf(currentBiometrics?.weightKg?.toString() ?: "70") }
    var heightStr by remember { mutableStateOf(currentBiometrics?.heightM?.toString() ?: "1.75") }
    var ageStr by remember { mutableStateOf(currentBiometrics?.age?.toString() ?: "25") }
    var isMale by remember { mutableStateOf(currentBiometrics?.isMale ?: true) }
    var activityFactor by remember { mutableStateOf(currentBiometrics?.activityFactor ?: 1.375) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "PARÁMETROS CORPORALES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Género
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { isMale = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMale) GymOrange else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isMale) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hombre")
                        }

                        Button(
                            onClick = { isMale = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isMale) GymOrange else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isMale) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mujer")
                        }
                    }
                }

                item {
                    // Peso
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Peso Corporal (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Altura
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("Altura (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 1.78") }
                    )
                }

                item {
                    // Edad
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("Edad (años)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Nivel de actividad predeterminado (Si no se calcula dinámicamente)
                    Text("Nivel de Actividad Manual (Mifflin-St Jeor)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    
                    val factors = listOf(
                        Pair(1.2, "Sedentario (Poco/sin ejercicio)"),
                        Pair(1.375, "Ligero (1-3 días de entreno/sem)"),
                        Pair(1.55, "Moderado (3-5 días de entreno/sem)"),
                        Pair(1.725, "Muy Activo (6-7 días de entreno/sem)")
                    )

                    Column {
                        factors.forEach { f ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activityFactor = f.first }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = activityFactor == f.first,
                                    onClick = { activityFactor = f.first },
                                    colors = RadioButtonDefaults.colors(selectedColor = GymOrange)
                                )
                                Text(f.second, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCELAR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val w = weightStr.toDoubleOrNull() ?: 70.0
                                val h = heightStr.toDoubleOrNull() ?: 1.75
                                val a = ageStr.toIntOrNull() ?: 25
                                onSave(w, h, a, isMale, activityFactor)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymOrange)
                        ) {
                            Text("GUARDAR CAMBIOS")
                        }
                    }
                }
            }
        }
    }
}
