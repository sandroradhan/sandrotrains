package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.GymOrange
import com.example.ui.components.bounceClickable
import com.example.ui.components.scaleSelected
import com.example.ui.components.borderShader
import com.example.ui.components.glowShader
import com.example.ui.theme.GymRed
import com.example.ui.theme.GymGreen
import com.example.data.ProgressPhoto
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.lazy.LazyRow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val biometricsState by viewModel.biometrics.collectAsState()
    val calendarDaysState by viewModel.calendarDays.collectAsState()
    val progressPhotosState by viewModel.progressPhotos.collectAsState()
    
    val context = LocalContext.current
    
    // Progress Photo States
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var photoDateInput by remember { mutableStateOf(WorkoutViewModel.getFormattedToday()) }
    var photoNoteInput by remember { mutableStateOf("") }
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    
    val photoTemplates = listOf(
        "template_chest" to "Torso / Pectoral",
        "template_back" to "Espalda / Dorsal",
        "template_legs" to "Piernas",
        "template_biceps" to "Brazos / Bíceps",
        "template_general" to "Gasto general"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val outputDir = File(context.filesDir, "progress_photos").apply { mkdirs() }
                val outputFile = File(outputDir, "photo_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(outputFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.addProgressPhoto(photoDateInput, outputFile.absolutePath, photoNoteInput)
                showAddPhotoDialog = false
                photoNoteInput = ""
                Toast.makeText(context, "¡Foto de progreso añadida con éxito!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error guardando imagen: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Form States
    var weightInput by remember(biometricsState) { 
        mutableStateOf(biometricsState?.weightKg?.toString() ?: "75.0") 
    }
    var heightInput by remember(biometricsState) { 
        mutableStateOf(biometricsState?.heightM?.toString() ?: "1.78") 
    }
    var ageInput by remember(biometricsState) { 
        mutableStateOf(biometricsState?.age?.toString() ?: "26") 
    }
    var isMale by remember(biometricsState) { 
        mutableStateOf(biometricsState?.isMale ?: true) 
    }
    var activityFactor by remember(biometricsState) { 
        mutableStateOf(biometricsState?.activityFactor ?: 1.375) 
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp)
    ) {
        // HEADER
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "SANDROTRAINS ATHLETE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8), // slate-400
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Mi Perfil Físico",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configura tus parámetros corporales para recalcular tu metabolismo basal y asimilar tu gasto calórico diario.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }

        // AVATAR & QUICK STATS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Modern Avatar Circle with Accent Rim
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(GymOrange.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, GymOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMale) Icons.Default.DirectionsRun else Icons.Default.Accessibility,
                            contentDescription = "Avatar",
                            tint = GymOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ATLETA SANDROTRAINS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isMale) "Masculino" else "Femenino",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        )

                        // BMI Estimation badge
                        val weight = weightInput.toDoubleOrNull() ?: 75.0
                        val height = heightInput.toDoubleOrNull() ?: 1.78
                        val bmiVal = if (height > 0) weight / (height * height) else 0.0
                        val bmiText = String.format(Locale.US, "%.1f", bmiVal)
                        val bmiCategory = when {
                            bmiVal < 18.5 -> "Bajo Peso"
                            bmiVal < 25.0 -> "Normopeso"
                            bmiVal < 30.0 -> "Sobrepeso"
                            else -> "Obesidad"
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IMC: $bmiText ($bmiCategory)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (bmiVal in 18.5..24.9) GymGreen else GymOrange
                                ),
                                modifier = Modifier
                                    .background(
                                        color = (if (bmiVal in 18.5..24.9) GymGreen else GymOrange).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // METABOLIC LIVE ESTIMATION
        item {
            biometricsState?.let { bio ->
                val dynamicFactor = viewModel.getDynamicActivityFactor(calendarDaysState)
                val tdee = viewModel.calculateDailyExpenditure(bio, dynamicFactor)
                val bmr = viewModel.calculateBMR(bio)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GymOrange, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = "${bmr.roundToInt()} kcal",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color.White)
                                )
                                Text("Consumo Basal (TMB)", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = GymOrange),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = "${tdee.roundToInt()} kcal",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color.Black)
                                )
                                Text("Gasto Diario (TDEE)", style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        // BIOMETRIC PROFILE FORM
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FORMULARIO BIOMÉTRICO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GymOrange,
                            letterSpacing = 1.sp
                        )
                    )

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

                    // Gender Selector (Male / Female)
                    Column {
                        Text(
                            text = "Género Corporal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isMale = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMale) GymOrange else Color.White.copy(alpha = 0.05f),
                                    contentColor = if (isMale) Color.Black else Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Male, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Masculino", fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { isMale = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isMale) GymOrange else Color.White.copy(alpha = 0.05f),
                                    contentColor = if (!isMale) Color.Black else Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Female, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Femenino", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Weight and Height row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Peso (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_weight_input")
                        )

                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Altura (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input")
                        )
                    }

                    // Age
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Edad (años)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_age_input")
                    )

                    // Activity Level Segment Selector
                    Column {
                        Text(
                            text = "Factor de Actividad Base",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        val levels = listOf(
                            1.2 to "Sedentario",
                            1.375 to "Ligero",
                            1.55 to "Moderado",
                            1.725 to "Activo"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            levels.forEach { (factor, name) ->
                                val isSelected = activityFactor == factor
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GymOrange else Color.White.copy(alpha = 0.05f))
                                        .border(
                                            if (isSelected) BorderStroke(0.dp, Color.Transparent) 
                                            else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .scaleSelected(isSelected)
                                        .bounceClickable { activityFactor = factor },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull()
                            val h = heightInput.toDoubleOrNull()
                            val a = ageInput.toIntOrNull()
                            
                            if (w != null && h != null && a != null) {
                                viewModel.updateBiometrics(
                                    weight = w,
                                    height = h,
                                    age = a,
                                    isMale = isMale,
                                    activityFactor = activityFactor
                                )
                                Toast.makeText(context, "Perfil físico guardado con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymOrange,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GUARDAR Y RECALCULAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECCIÓN FOTOS DE PROGRESO
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTORIAL VISUAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Fotos de Progreso",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                
                Button(
                    onClick = { showAddPhotoDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto de progreso", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REGISTRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (progressPhotosState.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Sin fotos",
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Aún no has registrado fotos de progreso corporal.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Sube una foto real o genera una de las plantillas de entrenamiento para ver tu progreso.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.25f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(progressPhotosState.size) { index ->
                        val photo = progressPhotosState[index]
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .height(210.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Dibujar o cargar la imagen
                                if (photo.imagePath.startsWith("template_")) {
                                    // Render del template con gradiente
                                    val gradientColors = when (photo.imagePath) {
                                        "template_chest" -> listOf(Color(0xFF1E293B), Color(0xFFEF4444))
                                        "template_back" -> listOf(Color(0xFF1E293B), Color(0xFFF97316))
                                        "template_legs" -> listOf(Color(0xFF1E293B), Color(0xFF3B82F6))
                                        "template_biceps" -> listOf(Color(0xFF1E293B), Color(0xFF10B981))
                                        else -> listOf(Color(0xFF1E293B), Color(0xFF8B5CF6))
                                    }
                                    val icon = when (photo.imagePath) {
                                        "template_chest" -> Icons.Default.AccessibilityNew
                                        "template_back" -> Icons.Default.VerticalSplit
                                        "template_legs" -> Icons.Default.DirectionsRun
                                        "template_biceps" -> Icons.Default.FitnessCenter
                                        else -> Icons.Default.ShowChart
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(gradientColors)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(icon, contentDescription = null, tint = GymOrange, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = photo.note.uppercase(Locale.getDefault()),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                } else {
                                    // Cargar la imagen del archivo local real
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(File(photo.imagePath))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Foto de progreso",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Sombrear la parte inferior para que el texto sea legible
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .height(65.dp)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                )

                                // Mostrar datos encima
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = photo.date,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymOrange
                                    )
                                    Text(
                                        text = if (photo.imagePath.startsWith("template_")) "Entrenamiento" else photo.note,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }

                                // Botón eliminar en la esquina superior derecha
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                        .clickable {
                                            if (!photo.imagePath.startsWith("template_")) {
                                                try {
                                                    File(photo.imagePath).delete()
                                                } catch (e: Exception) {
                                                    // No-op
                                                }
                                            }
                                            viewModel.deleteProgressPhoto(photo)
                                            Toast.makeText(context, "Foto eliminada con éxito", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Eliminar de progreso", 
                                        tint = GymRed, 
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para registrar una foto
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPhotoDialog = false },
            title = {
                Text(
                    "REGISTRAR HISTORIAL VISUAL", 
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = GymOrange
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Ingresa la fecha correspondiente, añade un comentario sobre tus sensaciones, y sube una foto real o genera un hito visual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = photoDateInput,
                        onValueChange = { photoDateInput = it },
                        label = { Text("Fecha (AAAA-MM-DD)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = photoNoteInput,
                        onValueChange = { photoNoteInput = it },
                        label = { Text("Nota (ej: Press de Banca 100kg, Abdominales)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Seleccionar plantilla si deseas simular:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Template choice chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(photoTemplates.size) { index ->
                            val (templateId, templateName) = photoTemplates[index]
                            val isSelected = selectedTemplateIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GymOrange else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSelected) GymOrange else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .bounceClickable { selectedTemplateIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = templateName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sube Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val templateId = photoTemplates[selectedTemplateIndex].first
                            val templateLabel = photoTemplates[selectedTemplateIndex].second
                            val finalNote = photoNoteInput.ifEmpty { templateLabel }
                            viewModel.addProgressPhoto(photoDateInput, templateId, finalNote)
                            showAddPhotoDialog = false
                            photoNoteInput = ""
                            Toast.makeText(context, "Hito visual guardado hoy con éxito", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Genera Hito", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotoDialog = false }) {
                    Text("Cancelar", color = GymRed, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A), // Slate-900 / Deep dark
            shape = RoundedCornerShape(24.dp)
        )
    }
}
