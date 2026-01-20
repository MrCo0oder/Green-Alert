package com.example.greenalert.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val radiusMeters: Float = 200f,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
