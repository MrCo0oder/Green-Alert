package com.example.greenalert.util

import android.content.Intent
import android.net.Uri
import java.util.regex.Pattern

object MapShareParser {
    
    /**
     * Parse shared content from Google Maps to extract coordinates or location name.
     * 
     * Supports:
     * - geo: URIs (geo:lat,lng or geo:0,0?q=address)
     * - Google Maps share links (maps.google.com, goo.gl/maps, etc.)
     * - Plain text with coordinates
     */
    fun parseSharedContent(intent: Intent): ParsedLocation? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                sharedText?.let { parseText(it) }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.let { parseGeoUri(it) }
            }
            else -> null
        }
    }
    
    private fun parseText(text: String): ParsedLocation? {
        // Try to extract coordinates from Google Maps URLs
        // Pattern: @lat,lng or /place/lat,lng
        val coordPattern = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val coordMatcher = coordPattern.matcher(text)
        
        if (coordMatcher.find()) {
            val lat = coordMatcher.group(1)?.toDoubleOrNull()
            val lng = coordMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                return ParsedLocation.Coordinates(lat, lng)
            }
        }
        
        // Try to extract place name from URL
        val placePattern = Pattern.compile("/place/([^/@]+)")
        val placeMatcher = placePattern.matcher(text)
        
        if (placeMatcher.find()) {
            val placeName = placeMatcher.group(1)?.replace("+", " ")
            if (!placeName.isNullOrBlank()) {
                return ParsedLocation.Address(Uri.decode(placeName))
            }
        }
        
        // If it looks like an address, return it
        if (!text.contains("http") && text.length > 5) {
            return ParsedLocation.Address(text.trim())
        }
        
        return null
    }
    
    private fun parseGeoUri(uri: Uri): ParsedLocation? {
        if (uri.scheme != "geo") return null
        
        val ssp = uri.schemeSpecificPart
        
        // Format: geo:lat,lng or geo:lat,lng?z=zoom
        val coordPattern = Pattern.compile("^(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        val coordMatcher = coordPattern.matcher(ssp)
        
        if (coordMatcher.find()) {
            val lat = coordMatcher.group(1)?.toDoubleOrNull()
            val lng = coordMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
                return ParsedLocation.Coordinates(lat, lng)
            }
        }
        
        // Format: geo:0,0?q=address or geo:0,0?q=lat,lng(label)
        val query = uri.getQueryParameter("q")
        if (!query.isNullOrBlank()) {
            // Check if query contains coordinates
            val queryCoordPattern = Pattern.compile("^(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
            val queryCoordMatcher = queryCoordPattern.matcher(query)
            
            if (queryCoordMatcher.find()) {
                val lat = queryCoordMatcher.group(1)?.toDoubleOrNull()
                val lng = queryCoordMatcher.group(2)?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    return ParsedLocation.Coordinates(lat, lng)
                }
            }
            
            // Otherwise treat as address
            return ParsedLocation.Address(query)
        }
        
        return null
    }
    
    sealed class ParsedLocation {
        data class Coordinates(val latitude: Double, val longitude: Double) : ParsedLocation()
        data class Address(val address: String) : ParsedLocation()
    }
}
