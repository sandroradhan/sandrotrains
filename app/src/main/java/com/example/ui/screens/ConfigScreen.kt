package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.GymOrange
import com.example.ui.theme.GymRed
import com.example.ui.theme.GymGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Core states for configuration choices
    var useLbs by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var backgroundSync by remember { mutableStateOf(false) }
    var timerSecondsIncrement by remember { mutableStateOf(5f) }

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
                    text = "SYSTEM PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8), // slate-400
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Modifica los ajustes operativos de sandrotrains para moldear la bitácora según tus hábitos olímpicos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }

        // PREFERENCES CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "AJUSTES DE LOGS & MÉTRICAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GymOrange,
                            letterSpacing = 1.sp
                        )
                    )

                    // Unified Kg vs Lbs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sistema de Peso Alternativo (Lbs)", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Cambia las mediciones de kilogramos (kg) a libras (lbs).", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = useLbs,
                            onCheckedChange = { 
                                useLbs = it
                                Toast.makeText(context, if (useLbs) "Métricas cambiadas a Libras (lbs)" else "Métricas cambiadas a Kilogramos (kg)", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GymOrange,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.testTag("switch_weight_unit")
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Sound Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerta de Cronómetro", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Emitir tono sonoro cuando el descanso de serie finalice.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GymOrange,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Vibration Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibración Háptica", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Vibrar el dispositivo móvil al terminar el descanso.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GymOrange,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Background synchronization Mock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sincronización en la Nube", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Habilitar guardado en la nube automático de SandroTrains.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = backgroundSync,
                            onCheckedChange = { 
                                backgroundSync = it 
                                if (backgroundSync) {
                                    Toast.makeText(context, "Sincronización segura habilitada", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GymOrange,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Restoration/Buffer Increments Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Incremento de Añadido Temporal", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${timerSecondsIncrement.toInt()}s", fontWeight = FontWeight.Black, color = GymOrange)
                        }
                        Text("Mapeo de segundos a sumar por toque interactivo sobre el reloj.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        Slider(
                            value = timerSecondsIncrement,
                            onValueChange = { timerSecondsIncrement = it },
                            valueRange = 5f..30f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = GymOrange,
                                activeTrackColor = GymOrange,
                                inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // DATABASE SEED AND DATA INTEGRITY
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "INTEGRIDAD DE DATOS LOCALES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GymRed,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "Si deseas iniciar una bitácora limpia o borrar todo tu historial guardado temporalmente, presiona el botón para borrar de forma segura todas las rutinas, ejercicios e historial.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )

                    Button(
                        onClick = {
                            viewModel.clearAllData()
                            Toast.makeText(context, "SandroTrains: Base de datos limpia e inicializada", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymRed.copy(alpha = 0.15f),
                            contentColor = GymRed
                        ),
                        border = BorderStroke(1.dp, GymRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("button_clear_database")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BORRAR TODA LA INFORMACIÓN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // ABOUT CREDS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Seal",
                        tint = GymOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "sandrotrains v1.0.0",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Producido en colaboración con Sandro de acuerdo a la carta de entrenamiento y especificaciones de sobrecarga progresiva.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
