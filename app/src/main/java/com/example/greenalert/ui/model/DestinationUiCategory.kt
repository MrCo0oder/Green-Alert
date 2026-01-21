package com.example.greenalert.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * UI-specific category enum for destinations.
 * Contains display properties like icons and colors for the UI layer only.
 * The data layer stores only the category name as a String.
 */
enum class DestinationUiCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    HOME("Home", Icons.Default.Home, Color(0xFF4CAF50)),
    WORK("Work", Icons.Default.Work, Color(0xFF2196F3)),
    SCHOOL("School", Icons.Default.School, Color(0xFFFF9800)),
    SHOPPING("Shopping", Icons.Default.ShoppingCart, Color(0xFFE91E63)),
    GYM("Gym", Icons.Default.FitnessCenter, Color(0xFF9C27B0)),
    RESTAURANT("Restaurant", Icons.Default.Restaurant, Color(0xFFFF5722)),
    HOSPITAL("Hospital", Icons.Default.LocalHospital, Color(0xFFF44336)),
    AIRPORT("Airport", Icons.Default.Flight, Color(0xFF00BCD4)),
    OTHER("Other", Icons.Default.Place, Color(0xFF607D8B));

    companion object {
        /**
         * Maps a category name string to the corresponding UI category enum.
         * Uses values() instead of entries for Kotlin version compatibility.
         * Returns OTHER if the name doesn't match any known category.
         */
        fun fromName(name: String): DestinationUiCategory {
            return values().find { it.name == name } ?: OTHER
        }
    }
}

/**
 * Extension function to convert a String to DestinationUiCategory.
 * Returns OTHER if the string doesn't match any known category name.
 */
fun String.toUiCategory(): DestinationUiCategory = DestinationUiCategory.fromName(this)
