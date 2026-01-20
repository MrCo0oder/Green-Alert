package com.example.greenalert.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.repository.DestinationRepository

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var destinationRepository: DestinationRepository
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = preferencesManager.userPreferences.first()
                
                if (prefs.trackingEnabled) {
                    // Re-register all active geofences after boot
                    val activeDestinations = destinationRepository.getActiveDestinationsList()
                    geofenceManager.addAllGeofences(activeDestinations)
                    
                    // Start the foreground service
                    val serviceIntent = Intent(context, LocationService::class.java).apply {
                        action = LocationService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
