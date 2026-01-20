package com.example.greenalert.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != GeofenceManager.ACTION_GEOFENCE_EVENT) return
        
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null")
            return
        }
        
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }
        
        val geofenceTransition = geofencingEvent.geofenceTransition
        
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            
            triggeringGeofences?.forEach { geofence ->
                val destinationId = geofence.requestId.toLongOrNull() ?: return@forEach
                Log.d(TAG, "Arrived at destination ID: $destinationId")
                
                // Show arrival notification
                notificationHelper.showArrivalNotification(destinationId)
            }
        }
    }
    
    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}
