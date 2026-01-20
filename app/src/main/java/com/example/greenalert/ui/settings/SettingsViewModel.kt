package com.example.greenalert.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val preferences: StateFlow<UserPreferences> = preferencesManager.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )
    
    fun setDefaultRadius(radius: Float) {
        viewModelScope.launch {
            preferencesManager.setDefaultRadius(radius)
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(enabled)
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVibrationEnabled(enabled)
        }
    }
}
