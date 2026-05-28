package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.WorkoutViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyReminder(context)
            Log.d("ReminderReceiver", "BOOT_COMPLETED received, rescheduled reminder.")
            return
        }

        // Trigger notification check in a safe I/O coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val todayStr = WorkoutViewModel.getFormattedToday()
                val todayRecord = db.workoutDao.getCalendarDayByDate(todayStr)

                val didTrainToday = todayRecord?.didTrain ?: false

                if (!didTrainToday) {
                    showNotification(context)
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error checking training status: ${e.message}")
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "sandro_trains_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Entrenamiento",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para motivarte a entrenar"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            120,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Seguro vector de android
            .setContentTitle("SandroTrains")
            .setContentText("hola sandro, te recuerdo que no has entrenado hoy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(777, notification)
    }

    companion object {
        fun scheduleDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                450,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                // Programar a las 20:00 (8:00 PM) para el recordatorio nocturno
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // Si la hora de hoy ya pasó de las 20:00, programar para mañana
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                // Alarma inexacta repetitiva amigable con la batería y CPU-Wakeup con pantalla apagada
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d("ReminderReceiver", "Scheduled daily training reminder at: ${calendar.time}")
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Failed to schedule alarm: ${e.message}")
            }
        }
    }
}
