package com.example.greenalert.data.repository

import com.example.greenalert.data.local.DestinationDao
import com.example.greenalert.data.model.Destination
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DestinationRepository @Inject constructor(
    private val destinationDao: DestinationDao
) {
    fun getAllDestinations(): Flow<List<Destination>> = destinationDao.getAllDestinations()
    
    fun getActiveDestinations(): Flow<List<Destination>> = destinationDao.getActiveDestinations()
    
    suspend fun getActiveDestinationsList(): List<Destination> = destinationDao.getActiveDestinationsList()
    
    suspend fun getDestinationById(id: Long): Destination? = destinationDao.getDestinationById(id)
    
    suspend fun addDestination(destination: Destination): Long = destinationDao.insertDestination(destination)
    
    suspend fun updateDestination(destination: Destination) = destinationDao.updateDestination(destination)
    
    suspend fun deleteDestination(destination: Destination) = destinationDao.deleteDestination(destination)
    
    suspend fun toggleDestinationActive(id: Long, isActive: Boolean) = destinationDao.setDestinationActive(id, isActive)
}
