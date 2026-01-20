package com.example.greenalert.ui.edit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenalert.data.model.Destination
import com.example.greenalert.data.model.DestinationCategory
import com.example.greenalert.data.repository.DestinationRepository
import com.example.greenalert.service.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditDestinationUiState(
    val destinationId: Long = 0,
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 200f,
    val category: DestinationCategory = DestinationCategory.OTHER,
    val isActive: Boolean = true,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditDestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val geofenceManager: GeofenceManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val destinationId: Long = savedStateHandle.get<Long>("destinationId") ?: 0L

    private val _uiState = MutableStateFlow(EditDestinationUiState())
    val uiState: StateFlow<EditDestinationUiState> = _uiState.asStateFlow()

    init {
        loadDestination()
    }

    private fun loadDestination() {
        viewModelScope.launch {
            val destination = destinationRepository.getDestinationById(destinationId)
            if (destination != null) {
                _uiState.update {
                    it.copy(
                        destinationId = destination.id,
                        name = destination.name,
                        address = destination.address,
                        latitude = destination.latitude,
                        longitude = destination.longitude,
                        radiusMeters = destination.radiusMeters,
                        category = destination.categoryEnum,
                        isActive = destination.isActive,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Destination not found")
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onRadiusChanged(radius: Float) {
        _uiState.update { it.copy(radiusMeters = radius) }
    }

    fun onCategoryChanged(category: DestinationCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun saveDestination() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val updatedDestination = Destination(
                id = state.destinationId,
                name = state.name,
                latitude = state.latitude,
                longitude = state.longitude,
                address = state.address,
                radiusMeters = state.radiusMeters,
                category = state.category.name,
                isActive = state.isActive
            )

            destinationRepository.updateDestination(updatedDestination)

            // Update geofence if active
            if (state.isActive) {
                geofenceManager.removeGeofence(state.destinationId)
                geofenceManager.addGeofence(updatedDestination)
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteDestination() {
        viewModelScope.launch {
            val state = _uiState.value
            geofenceManager.removeGeofence(state.destinationId)

            val destination = destinationRepository.getDestinationById(state.destinationId)
            destination?.let {
                destinationRepository.deleteDestination(it)
            }

            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
