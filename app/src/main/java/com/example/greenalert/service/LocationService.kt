package com.example.greenalert.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.greenalert.data.repository.DestinationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @Inject
    lateinit var destinationRepository: DestinationRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }
    
    private fun startService() {
        Log.d(TAG, "Starting location service")
        
        val notification = notificationHelper.getServiceNotification()
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        
        // Register geofences for all active destinations
        serviceScope.launch {
            val activeDestinations = destinationRepository.getActiveDestinationsList()
            geofenceManager.addAllGeofences(activeDestinations)
            Log.d(TAG, "Registered ${activeDestinations.size} geofences")
        }
    }
    
    private fun stopService() {
        Log.d(TAG, "Stopping location service")
        geofenceManager.removeAllGeofences()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "LocationService destroyed")
    }
    
    companion object {
        private const val TAG = "LocationService"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
