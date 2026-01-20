package com.example.greenalert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.greenalert.MainActivity
import com.example.greenalert.R
import com.example.greenalert.data.local.DestinationDao
import com.example.greenalert.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val destinationDao: DestinationDao,
    private val preferencesManager: PreferencesManager
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Arrival alerts channel with sound
            val arrivalChannel = NotificationChannel(
                ARRIVAL_CHANNEL_ID,
                "Arrival Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you arrive at a destination"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Service channel (silent)
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when location tracking is active"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(arrivalChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
    
    fun showArrivalNotification(destinationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val destination = destinationDao.getDestinationById(destinationId) ?: return@launch
            val prefs = preferencesManager.userPreferences.first()
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                destinationId.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            
            val builder = NotificationCompat.Builder(context, ARRIVAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("You've Arrived! 🎯")
                .setContentText("Welcome to ${destination.name}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("You have arrived at ${destination.name}\n${destination.address}"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            
            // Handle sound based on preference
            if (!prefs.soundEnabled) {
                builder.setSound(null)
            }
            
            // Handle vibration based on preference
            if (prefs.vibrationEnabled) {
                triggerVibration()
            }
            
            notificationManager.notify(destinationId.toInt(), builder.build())
        }
    }
    
    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }
    
    fun getServiceNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle("Green Alert Active")
            .setContentText("Monitoring your destinations")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    companion object {
        const val ARRIVAL_CHANNEL_ID = "arrival_alerts"
        const val SERVICE_CHANNEL_ID = "location_service"
        const val SERVICE_NOTIFICATION_ID = 1001
    }
}
