package com.example.greenalert.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DestinationCategory(
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
        fun fromName(name: String): DestinationCategory {
            return entries.find { it.name == name } ?: OTHER
        }
    }
}

@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val radiusMeters: Float = 200f,
    val category: String = DestinationCategory.OTHER.name,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: DestinationCategory
        get() = DestinationCategory.fromName(category)
}
