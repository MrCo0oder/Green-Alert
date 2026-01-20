package com.example.greenalert.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.greenalert.data.model.Destination

@Database(
    entities = [Destination::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
}
