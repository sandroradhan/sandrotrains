package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.GymOrange
import com.example.ui.theme.GymRed
import com.example.ui.components.bounceClickable
import com.example.ui.components.glowShader
import com.example.ui.components.borderShader
import com.example.ui.WorkoutViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Composable
fun AiSearchScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val aiConversations by viewModel.aiConversations.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sugerencias de búsqueda rápida
    val quickSuggestions = listOf(
        "Técnica: Sentadilla Profunda",
        "Dolor de hombro en press militar",
        "Ejercicios alternativos para dominadas",
        "Explicación de Sobrecarga Progresiva"
    )

    fun performAiSearch(promptText: String) {
        if (promptText.trim().isEmpty()) {
            Toast.makeText(context, "Por favor escribe tu duda primero", Toast.LENGTH_SHORT).show()
            return
        }
        keyboardController?.hide()
        query = promptText
        isLoading = true
        resultText = ""
        coroutineScope.launch {
            val systemContext = "Eres un entrenador personal inteligente y experto en kinesiología de SandroTrains. " +
                    "Responde de forma clara, motivadora y precisa en español. Sé directo y estructurado usando viñetas donde aplique. " +
                    "La duda de tu atleta es: "
            val answer = askGemini(systemContext + promptText)
            resultText = answer
            isLoading = false
            if (answer.isNotEmpty() && !answer.startsWith("Error:") && !answer.startsWith("Error preparando")) {
                viewModel.addAiConversation(promptText, answer)
            }
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
        // CABECERA DE LA SECCIÓN
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "SANDROTRAINS IA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8), // slate-400
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Consultas Inteligentes",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Text(
                    text = "Resuelve tus dudas sobre técnicas de ejercicios, dolores comunes o alternativas usando Inteligencia Artificial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // BARRA DE BÚSQUEDA CON IA
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_search_bar_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Escribe ej: ¿Cómo ejecuto peso muerto correctamente?", color = Color.White.copy(alpha = 0.35f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GymOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.01f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                            focusedLabelColor = GymOrange,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ai_search_input_field"),
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performAiSearch(query) }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar texto", tint = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    )

                    Button(
                        onClick = { performAiSearch(query) },
                        colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_search_submit_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CONSULTAR A LA IA", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // CHIPS DE SUGERENCIA RÁPIDA
        item {
            Column {
                Text(
                    text = "Sugerencias rápidas de atletas:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(30.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(30.dp))
                                .bounceClickable(enabled = !isLoading) {
                                    performAiSearch(suggestion)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymOrange
                            )
                        }
                    }
                }
            }
        }

        // RESPUESTA DE LA IA
        item {
            AnimatedVisibility(
                visible = isLoading || resultText.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_result_card")
                        .borderShader(shape = RoundedCornerShape(24.dp), borderWidth = 1.5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GymOrange, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RESPUESTA ENTRENADOR IA",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GymOrange
                            )
                        }

                        if (isLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = GymOrange, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Analizando ejercicio en SandroTrains...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp
                                ),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GymOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠️ SandroTrains IA es una herramienta de asesoría general y no reemplaza los consejos de fisioterapeutas o entrenadores diplomados. Entrena de forma segura.",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    color = GymOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // HISTORIAL DE CONVERSACIONES
        if (aiConversations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HISTORIAL DE CONSULTAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    TextButton(
                        onClick = { viewModel.clearAiConversations() },
                        colors = ButtonDefaults.textButtonColors(contentColor = GymRed)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Borrar todo del historial", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar todo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(aiConversations.size) { index ->
                val convo = aiConversations[index]
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (expanded) GymOrange.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline, 
                                    contentDescription = "Pregunta", 
                                    tint = GymOrange, 
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = convo.question,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteAiConversation(convo) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Eliminar", 
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.AutoAwesome, 
                                    contentDescription = "Respuesta de la IA", 
                                    tint = GymOrange, 
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = convo.answer,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        } else {
                            Text(
                                text = "Toca para ver respuesta...",
                                fontSize = 11.sp,
                                color = GymOrange.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// REST Client Implementation for Gemini API (Option B) using native Android structures to avoid dependency hell
private suspend fun askGemini(prompt: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Error: No se encontró una clave de API de Gemini válida configurada en tu panel de Secrets."
    }

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Preparar el cuerpo del request usando JSONObject nativo de Android
    val requestJson = JSONObject()
    val contentsArray = JSONArray()
    val partsArray = JSONArray()
    val textObject = JSONObject()

    try {
        textObject.put("text", prompt)
        partsArray.put(textObject)
        
        val contentObject = JSONObject()
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        
        requestJson.put("contents", contentsArray)
    } catch (e: Exception) {
        return@withContext "Error preparando cuerpo del mensaje: ${e.localizedMessage}"
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext "Error devuelto por Gemini (${response.code}): ${response.message}. Revisa que tu API Key sea del plan correcto y activa."
            }
            val responseBody = response.body?.string() ?: return@withContext "Respuesta vacía del servidor."
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            "No se devolvió ningún texto en la respuesta."
        }
    } catch (e: Exception) {
        "Error de conexión con la IA de Google: ${e.localizedMessage}"
    }
}

// Simple FlowRow helper if not imported
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
