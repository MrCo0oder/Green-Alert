package com.example.greenalert.ui.manualinput

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenalert.data.model.Destination
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.repository.DestinationRepository
import com.example.greenalert.service.GeofenceManager
import com.example.greenalert.util.GeocodingResult
import com.example.greenalert.util.GeocodingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualInputUiState(
    val destinationName: String = "",
    val addressInput: String = "",
    val geocodedResult: GeocodingResult? = null,
    val radiusMeters: Float = 200f,
    val isGeocoding: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ManualInputViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val preferencesManager: PreferencesManager,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState: StateFlow<ManualInputUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            val defaultRadius = preferencesManager.userPreferences.first().defaultRadiusMeters
            _uiState.update { it.copy(radiusMeters = defaultRadius) }
        }
    }
    
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(destinationName = name) }
    }
    
    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(addressInput = address, geocodedResult = null) }
    }
    
    fun onRadiusChanged(radius: Float) {
        _uiState.update { it.copy(radiusMeters = radius) }
    }
    
    fun geocodeAddress() {
        val address = _uiState.value.addressInput
        if (address.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an address") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true, errorMessage = null) }
            
            val result = GeocodingUtils.geocodeAddress(context, address)
            
            if (result != null) {
                _uiState.update { 
                    it.copy(geocodedResult = result, isGeocoding = false)
                }
            } else {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Could not find location. Try a more specific address.",
                        isGeocoding = false
                    )
                }
            }
        }
    }
    
    fun saveDestination() {
        val state = _uiState.value
        val geocoded = state.geocodedResult
        
        if (geocoded == null) {
            _uiState.update { it.copy(errorMessage = "Please search for an address first") }
            return
        }
        
        val name = state.destinationName.ifBlank { geocoded.address.take(30) }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val destination = Destination(
                name = name,
                latitude = geocoded.latitude,
                longitude = geocoded.longitude,
                address = geocoded.address,
                radiusMeters = state.radiusMeters,
                isActive = true
            )
            
            val id = destinationRepository.addDestination(destination)
            
            // Add geofence if tracking is enabled
            val prefs = preferencesManager.userPreferences.first()
            if (prefs.trackingEnabled) {
                geofenceManager.addGeofence(destination.copy(id = id))
            }
            
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
    
    fun setInitialAddress(address: String) {
        _uiState.update { it.copy(addressInput = address) }
        geocodeAddress()
    }
    
    fun setInitialCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true) }
            
            val address = GeocodingUtils.reverseGeocode(context, latitude, longitude)
                ?: "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}"
            
            _uiState.update { 
                it.copy(
                    geocodedResult = GeocodingResult(latitude, longitude, address),
                    addressInput = address,
                    isGeocoding = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
