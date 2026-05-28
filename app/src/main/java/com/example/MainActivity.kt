package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.WorkoutRepository
import com.example.data.ReminderReceiver
import android.os.Build
import com.example.ui.WorkoutViewModel
import com.example.ui.WorkoutViewModelFactory
import com.example.ui.screens.ConfigScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.AiSearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.scaleSelected
import androidx.compose.material.icons.filled.AutoAwesome

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar alarmas de recordatorio de entrenamiento
        ReminderReceiver.scheduleDailyReminder(applicationContext)
        
        // Solicitar permisos de notificación en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        
        // Inicializar persistencia Room y Repositorio
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = WorkoutRepository(database.workoutDao)
        
        setContent {
            MyApplicationTheme {
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(repository)
                )
                
                var currentTab by remember { mutableStateOf(0) }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                label = { Text("Bitácora") },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                modifier = Modifier
                                    .testTag("nav_tab_dashboard")
                                    .scaleSelected(currentTab == 0)
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                label = { Text("Rutinas") },
                                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Routines") },
                                modifier = Modifier
                                    .testTag("nav_tab_routines")
                                    .scaleSelected(currentTab == 1)
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                label = { Text("Preguntas IA") },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Preguntas IA") },
                                modifier = Modifier
                                    .testTag("nav_tab_ai_search")
                                    .scaleSelected(currentTab == 2)
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                label = { Text("Perfil") },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                modifier = Modifier
                                    .testTag("nav_tab_profile")
                                    .scaleSelected(currentTab == 3)
                            )
                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                label = { Text("Config") },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                                modifier = Modifier
                                    .testTag("nav_tab_config")
                                    .scaleSelected(currentTab == 4)
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    when (currentTab) {
                        0 -> DashboardScreen(
                            viewModel = workoutViewModel,
                            modifier = screenModifier
                        )
                        1 -> RoutinesScreen(
                            viewModel = workoutViewModel,
                            modifier = screenModifier
                        )
                        2 -> AiSearchScreen(
                            viewModel = workoutViewModel,
                            modifier = screenModifier
                        )
                        3 -> ProfileScreen(
                            viewModel = workoutViewModel,
                            modifier = screenModifier
                        )
                        4 -> ConfigScreen(
                            viewModel = workoutViewModel,
                            modifier = screenModifier
                        )
                    }
                }
            }
        }
    }
}

