package com.example.greenalert.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class GeocodingResult(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

object GeocodingUtils {
    
    suspend fun geocodeAddress(context: Context, addressString: String): GeocodingResult? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocationName(addressString, 1) { addresses ->
                            val result = addresses.firstOrNull()?.toGeocodingResult()
                            continuation.resume(result)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(addressString, 1)
                    addresses?.firstOrNull()?.toGeocodingResult()
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()?.formatAddress()
                            continuation.resume(address)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.formatAddress()
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun Address.toGeocodingResult(): GeocodingResult {
        return GeocodingResult(
            latitude = latitude,
            longitude = longitude,
            address = formatAddress()
        )
    }
    
    private fun Address.formatAddress(): String {
        val parts = mutableListOf<String>()
        
        // Try to get a meaningful address
        if (!thoroughfare.isNullOrBlank()) parts.add(thoroughfare)
        if (!subLocality.isNullOrBlank()) parts.add(subLocality)
        if (!locality.isNullOrBlank()) parts.add(locality)
        if (!adminArea.isNullOrBlank()) parts.add(adminArea)
        if (!countryName.isNullOrBlank()) parts.add(countryName)
        
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            getAddressLine(0) ?: "Unknown location"
        }
    }
}
