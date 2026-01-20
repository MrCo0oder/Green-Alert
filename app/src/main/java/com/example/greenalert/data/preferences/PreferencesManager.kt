package com.example.greenalert.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPreferences(
    val defaultRadiusMeters: Float = 200f,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val trackingEnabled: Boolean = false
)

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val DEFAULT_RADIUS = floatPreferencesKey("default_radius")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val TRACKING_ENABLED = booleanPreferencesKey("tracking_enabled")
    }
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                defaultRadiusMeters = preferences[Keys.DEFAULT_RADIUS] ?: 200f,
                soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true,
                trackingEnabled = preferences[Keys.TRACKING_ENABLED] ?: false
            )
        }
    
    suspend fun setDefaultRadius(radius: Float) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_RADIUS] = radius
        }
    }
    
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SOUND_ENABLED] = enabled
        }
    }
    
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.VIBRATION_ENABLED] = enabled
        }
    }
    
    suspend fun setTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TRACKING_ENABLED] = enabled
        }
    }
}
