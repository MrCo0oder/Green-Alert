package com.example.greenalert.ui.mappicker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenalert.data.model.Destination
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.repository.DestinationRepository
import com.example.greenalert.service.GeofenceManager
import com.example.greenalert.util.GeocodingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapPickerUiState(
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null,
    val selectedAddress: String = "",
    val destinationName: String = "",
    val radiusMeters: Float = 200f,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MapPickerViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val preferencesManager: PreferencesManager,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapPickerUiState())
    val uiState: StateFlow<MapPickerUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            val defaultRadius = preferencesManager.userPreferences.first().defaultRadiusMeters
            _uiState.update { it.copy(radiusMeters = defaultRadius) }
        }
    }
    
    fun onLocationSelected(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    selectedLatitude = latitude,
                    selectedLongitude = longitude,
                    isLoading = true
                )
            }
            
            val address = GeocodingUtils.reverseGeocode(context, latitude, longitude)
                ?: "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}"
            
            _uiState.update { 
                it.copy(
                    selectedAddress = address,
                    isLoading = false
                )
            }
        }
    }
    
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(destinationName = name) }
    }
    
    fun onRadiusChanged(radius: Float) {
        _uiState.update { it.copy(radiusMeters = radius) }
    }
    
    fun saveDestination() {
        val state = _uiState.value
        
        if (state.selectedLatitude == null || state.selectedLongitude == null) {
            _uiState.update { it.copy(errorMessage = "Please select a location on the map") }
            return
        }
        
        val name = state.destinationName.ifBlank { state.selectedAddress.take(30) }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val destination = Destination(
                name = name,
                latitude = state.selectedLatitude,
                longitude = state.selectedLongitude,
                address = state.selectedAddress,
                radiusMeters = state.radiusMeters,
                isActive = true
            )
            
            val id = destinationRepository.addDestination(destination)
            
            // Add geofence if tracking is enabled
            val prefs = preferencesManager.userPreferences.first()
            if (prefs.trackingEnabled) {
                geofenceManager.addGeofence(destination.copy(id = id))
            }
            
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
