package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Exercise
import com.example.data.Routine
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.GymOrange
import com.example.ui.theme.GymRed
import com.example.ui.components.bounceClickable
import com.example.ui.components.scaleSelected
import com.example.ui.components.borderShader
import com.example.ui.components.glowShader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val selectedRoutineId by viewModel.selectedRoutineId.collectAsState()
    val exercises by viewModel.selectedRoutineExercises.collectAsState()

    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    val activeRoutine = remember(routines, selectedRoutineId) {
        routines.find { it.id == selectedRoutineId }
    }

    // Para la cuenta interactiva de temporizadores de descanso independientes por ejercicio
    val activeTimers = remember { mutableStateMapOf<Long, Int>() } // exerciseId -> remainingSeconds
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
    ) {
        // CABECERA DE LA SECCIÓN
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GESTIÓN DE RUTINAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Tus Divisiones",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                Button(
                    onClick = { showAddRoutineDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva", fontWeight = FontWeight.Bold)
                }
            }
        }

        // SELECTOR DE RUTINA ACTIVA
        item {
            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se han registrado rutinas libres todavía.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    text = "Selecciona Rutina:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    routines.forEach { r ->
                        val isSelected = r.id == selectedRoutineId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scaleSelected(isSelected)
                                .then(if (isSelected) Modifier.borderShader(RoundedCornerShape(16.dp)) else Modifier)
                                .bounceClickable { viewModel.selectRoutine(r.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GymOrange.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.name.uppercase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) GymOrange else Color.White
                                    )
                                    if (r.description.isNotEmpty()) {
                                        Text(
                                            text = r.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) GymOrange.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.4f)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { editingRoutine = r }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar rutina", tint = GymOrange.copy(alpha = 0.8f))
                                    }
                                    if (!isSelected) {
                                        IconButton(
                                            onClick = { viewModel.deleteRoutine(r) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar rutina", tint = GymRed.copy(alpha = 0.7f))
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Seleccionada",
                                            tint = GymOrange,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECCIÓN EJERCICIOS
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ejercicios Listados (${exercises.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )

                if (selectedRoutineId != null) {
                    TextButton(
                        onClick = { showAddExerciseDialog = true },
                        colors = ButtonColors(containerColor = Color.Transparent, contentColor = GymOrange, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp), tint = GymOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Ejercicio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (exercises.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GymOrange, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Esta rutina no tiene ejercicios asignados", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text("Utiliza el botón 'Añadir Ejercicio' para comenzar a enriquecer tu sesión.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        } else {
            items(exercises, key = { it.id }) { exercise ->
                // Estado del timer de descanso
                val remainingSeconds = activeTimers[exercise.id] ?: 0

                // Campos para registrar progreso inline
                var inlineWeight by remember { mutableStateOf("") }
                var inlineReps by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Ilustración del Ejercicio (Coil AsyncImage con Fallback visual)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(exercise.illustrationUrl)
                                    .crossfade(true)
                                    .build(),
                                placeholder = null,
                                contentDescription = "Técnica del ejercicio",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f)),
                                onError = {
                                    // Fallback opcional cargando imagen por defecto
                                }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Especificaciones
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${exercise.sets} Series × ${exercise.reps} Reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GymOrange
                                )
                                Text(
                                    text = "Descanso recomendado: ${exercise.restSeconds}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }

                            // Botones Editar y Borrar
                            Row(
                                modifier = Modifier.align(Alignment.Top),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { editingExercise = exercise }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Ejercicio", tint = GymOrange.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.deleteExercise(exercise) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Ejercicio", tint = GymRed.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // INTERFAZ DE TEMPORIZADOR DE DESCANSO INTERACTIVO
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TEMPORIZADOR DE DESCANSO", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GymOrange)
                                Text(
                                    text = if (remainingSeconds > 0) "Descansando... ${remainingSeconds}s" else "Apuntado para descansar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    if (remainingSeconds > 0) {
                                        activeTimers[exercise.id] = 0 // Detener
                                    } else {
                                        // Iniciar temporizador regresivo de descanso
                                        activeTimers[exercise.id] = exercise.restSeconds
                                        coroutineScope.launch {
                                            while (activeTimers[exercise.id] ?: 0 > 0) {
                                                delay(1000)
                                                val curr = activeTimers[exercise.id] ?: 0
                                                if (curr > 0) {
                                                    activeTimers[exercise.id] = curr - 1
                                                }
                                            }
                                            if (activeTimers[exercise.id] == 0) {
                                                Toast.makeText(context, "¡Descanso terminado para ${exercise.name}!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (remainingSeconds > 0) GymRed else GymOrange,
                                    contentColor = if (remainingSeconds > 0) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (remainingSeconds > 0) Icons.Default.TimerOff else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (remainingSeconds > 0) "Parar" else "Iniciar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // LOG DE SERIE (INTEGRADO DE PROGRESO)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inlineWeight,
                                onValueChange = { inlineWeight = it },
                                label = { Text("Peso (kg)", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GymOrange,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = GymOrange,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = inlineReps,
                                onValueChange = { inlineReps = it },
                                label = { Text("Reps", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GymOrange,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = GymOrange,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val w = inlineWeight.toDoubleOrNull()
                                    val r = inlineReps.toIntOrNull()
                                    if (w != null && r != null) {
                                        val today = WorkoutViewModel.getFormattedToday()
                                        viewModel.addProgressRecord(exercise.id, w, r, today)
                                        Toast.makeText(context, "Carga registrada con éxito", Toast.LENGTH_SHORT).show()
                                        inlineWeight = ""
                                        inlineReps = ""
                                    } else {
                                        Toast.makeText(context, "Inserta valores numéricos válidos", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGO AÑADIR NUEVA RUTINA
    if (showAddRoutineDialog) {
        var rName by remember { mutableStateOf("") }
        var rDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddRoutineDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "NUEVA RUTINA DE ENTRENAMIENTO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = GymOrange,
                            letterSpacing = 0.5.sp
                        )
                    )

                    OutlinedTextField(
                        value = rName,
                        onValueChange = { rName = it },
                        label = { Text("Nombre de la Routine", color = Color.White.copy(alpha = 0.5f)) },
                        placeholder = { Text("Ej: Empuje, Fullbody B...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = GymOrange,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rDesc,
                        onValueChange = { rDesc = it },
                        label = { Text("Descripción / Enfoque", color = Color.White.copy(alpha = 0.5f)) },
                        placeholder = { Text("Ej: Fuerza pura, Híbrido, etc") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = GymOrange,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddRoutineDialog = false }) {
                            Text("CANCELAR", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rName.isNotEmpty()) {
                                    viewModel.addRoutine(rName, rDesc)
                                    showAddRoutineDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("AÑADIR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // DIALOGO AÑADIR NUEVA EXERCISE
    if (showAddExerciseDialog) {
        var eName by remember { mutableStateOf("") }
        var eSets by remember { mutableStateOf("4") }
        var eReps by remember { mutableStateOf("10") }
        var eRest by remember { mutableStateOf("90") }
        var eUrl by remember { mutableStateOf("") }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GymOrange,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = GymOrange,
            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
            focusedContainerColor = Color.White.copy(alpha = 0.02f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
        )

        Dialog(onDismissRequest = { showAddExerciseDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "AÑADIR EJERCICIO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GymOrange,
                                letterSpacing = 0.5.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eName,
                            onValueChange = { eName = it },
                            label = { Text("Nombre de Ejercicio", color = Color.White.copy(alpha = 0.5f)) },
                            placeholder = { Text("Ej: Zancadas con Mancuernas") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = eSets,
                                onValueChange = { eSets = it },
                                label = { Text("Series", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = eReps,
                                onValueChange = { eReps = it },
                                label = { Text("Repeticiones", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = eRest,
                            onValueChange = { eRest = it },
                            label = { Text("Segundos de descanso", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eUrl,
                            onValueChange = { eUrl = it },
                            label = { Text("URL de imagen / GIF de técnica", color = Color.White.copy(alpha = 0.5f)) },
                            placeholder = { Text("Opcional: URL de Unsplash...") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddExerciseDialog = false }) {
                                Text("CANCELAR", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (eName.isNotEmpty()) {
                                        viewModel.addExercise(
                                            name = eName,
                                            sets = eSets.toIntOrNull() ?: 4,
                                            reps = eReps.toIntOrNull() ?: 10,
                                            restSeconds = eRest.toIntOrNull() ?: 90,
                                            url = eUrl
                                        )
                                        showAddExerciseDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("AÑADIR EJERCICIO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGO EDITAR RUTINA
    if (editingRoutine != null) {
        val routineToEdit = editingRoutine!!
        var eName by remember(routineToEdit) { mutableStateOf(routineToEdit.name) }
        var eDesc by remember(routineToEdit) { mutableStateOf(routineToEdit.description) }

        Dialog(onDismissRequest = { editingRoutine = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "EDITAR RUTINA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = GymOrange,
                            letterSpacing = 0.5.sp
                        )
                    )

                    OutlinedTextField(
                        value = eName,
                        onValueChange = { eName = it },
                        label = { Text("Nombre de la Rutina", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = GymOrange,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_routine_name_field")
                    )

                    OutlinedTextField(
                        value = eDesc,
                        onValueChange = { eDesc = it },
                        label = { Text("Descripción / Enfoque", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = GymOrange,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { editingRoutine = null }) {
                            Text("CANCELAR", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (eName.isNotEmpty()) {
                                    viewModel.updateRoutine(
                                        routineToEdit.copy(name = eName, description = eDesc)
                                    )
                                    editingRoutine = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_routine_save_button")
                        ) {
                            Text("GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // DIALOGO EDITAR EJERCICIO
    if (editingExercise != null) {
        val exerciseToEdit = editingExercise!!
        var eName by remember(exerciseToEdit) { mutableStateOf(exerciseToEdit.name) }
        var eSets by remember(exerciseToEdit) { mutableStateOf(exerciseToEdit.sets.toString()) }
        var eReps by remember(exerciseToEdit) { mutableStateOf(exerciseToEdit.reps.toString()) }
        var eRest by remember(exerciseToEdit) { mutableStateOf(exerciseToEdit.restSeconds.toString()) }
        var eUrl by remember(exerciseToEdit) { mutableStateOf(exerciseToEdit.illustrationUrl) }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GymOrange,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = GymOrange,
            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
            focusedContainerColor = Color.White.copy(alpha = 0.02f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
        )

        Dialog(onDismissRequest = { editingExercise = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "EDITAR EJERCICIO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GymOrange,
                                letterSpacing = 0.5.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eName,
                            onValueChange = { eName = it },
                            label = { Text("Nombre de Ejercicio", color = Color.White.copy(alpha = 0.5f)) },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("edit_exercise_name_field")
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = eSets,
                                onValueChange = { eSets = it },
                                label = { Text("Series", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = eReps,
                                onValueChange = { eReps = it },
                                label = { Text("Repeticiones", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = eRest,
                            onValueChange = { eRest = it },
                            label = { Text("Segundos de descanso", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eUrl,
                            onValueChange = { eUrl = it },
                            label = { Text("URL de imagen / GIF de técnica", color = Color.White.copy(alpha = 0.5f)) },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { editingExercise = null }) {
                                Text("CANCELAR", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (eName.isNotEmpty()) {
                                        viewModel.updateExercise(
                                            exerciseToEdit.copy(
                                                name = eName,
                                                sets = eSets.toIntOrNull() ?: exerciseToEdit.sets,
                                                reps = eReps.toIntOrNull() ?: exerciseToEdit.reps,
                                                restSeconds = eRest.toIntOrNull() ?: exerciseToEdit.restSeconds,
                                                illustrationUrl = eUrl
                                            )
                                        )
                                        editingExercise = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("edit_exercise_save_button")
                            ) {
                                Text("GUARDAR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
