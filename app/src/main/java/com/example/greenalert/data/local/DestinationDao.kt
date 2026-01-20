package com.example.greenalert.data.local

import androidx.room.*
import com.example.greenalert.data.model.Destination
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {
    
    @Query("SELECT * FROM destinations ORDER BY createdAt DESC")
    fun getAllDestinations(): Flow<List<Destination>>
    
    @Query("SELECT * FROM destinations WHERE isActive = 1")
    fun getActiveDestinations(): Flow<List<Destination>>
    
    @Query("SELECT * FROM destinations WHERE isActive = 1")
    suspend fun getActiveDestinationsList(): List<Destination>
    
    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: Long): Destination?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: Destination): Long
    
    @Update
    suspend fun updateDestination(destination: Destination)
    
    @Delete
    suspend fun deleteDestination(destination: Destination)
    
    @Query("UPDATE destinations SET isActive = :isActive WHERE id = :id")
    suspend fun setDestinationActive(id: Long, isActive: Boolean)
}
