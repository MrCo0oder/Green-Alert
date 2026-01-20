package com.example.greenalert.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.greenalert.data.model.Destination
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    
    fun addGeofence(destination: Destination, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onFailure(SecurityException("Location permission not granted"))
            return
        }
        
        val geofence = Geofence.Builder()
            .setRequestId(destination.id.toString())
            .setCircularRegion(
                destination.latitude,
                destination.longitude,
                destination.radiusMeters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added for destination: ${destination.name}")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to add geofence: ${exception.message}")
                onFailure(exception)
            }
    }
    
    fun removeGeofence(destinationId: Long, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        geofencingClient.removeGeofences(listOf(destinationId.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence removed for destination: $destinationId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to remove geofence: ${exception.message}")
                onFailure(exception)
            }
    }
    
    fun addAllGeofences(destinations: List<Destination>) {
        destinations.filter { it.isActive }.forEach { destination ->
            addGeofence(destination)
        }
    }
    
    fun removeAllGeofences(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "All geofences removed")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to remove all geofences: ${exception.message}")
                onFailure(exception)
            }
    }
    
    companion object {
        private const val TAG = "GeofenceManager"
        const val ACTION_GEOFENCE_EVENT = "com.example.greenalert.ACTION_GEOFENCE_EVENT"
    }
}
