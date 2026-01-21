package com.example.greenalert.ui.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenalert.data.model.Destination
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.preferences.UserPreferences
import com.example.greenalert.data.repository.DestinationRepository
import com.example.greenalert.service.GeofenceManager
import com.example.greenalert.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val destinations: List<Destination> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val preferencesManager: PreferencesManager,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            combine(
                destinationRepository.getAllDestinations(),
                preferencesManager.userPreferences
            ) { destinations, preferences ->
                HomeUiState(
                    destinations = destinations,
                    preferences = preferences,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun toggleTracking(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setTrackingEnabled(enabled)
            
            val serviceIntent = Intent(context, LocationService::class.java)
            if (enabled) {
                serviceIntent.action = LocationService.ACTION_START
                context.startForegroundService(serviceIntent)
            } else {
                serviceIntent.action = LocationService.ACTION_STOP
                context.startService(serviceIntent)
            }
        }
    }
    
    fun toggleDestinationActive(destination: Destination, isActive: Boolean) {
        viewModelScope.launch {
            destinationRepository.toggleDestinationActive(destination.id, isActive)
            
            // Always remove old geofence first, then add only if active and tracking enabled
            geofenceManager.removeGeofence(destination.id)
            if (isActive && _uiState.value.preferences.trackingEnabled) {
                geofenceManager.addGeofence(destination.copy(isActive = true))
            }
        }
    }
    
    fun deleteDestination(destination: Destination) {
        viewModelScope.launch {
            geofenceManager.removeGeofence(destination.id)
            destinationRepository.deleteDestination(destination)
        }
    }
}
