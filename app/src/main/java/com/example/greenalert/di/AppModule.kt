package com.example.greenalert.di

import android.content.Context
import androidx.room.Room
import com.example.greenalert.data.local.AppDatabase
import com.example.greenalert.data.local.DestinationDao
import com.example.greenalert.data.preferences.PreferencesManager
import com.example.greenalert.data.repository.DestinationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "greenalert_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideDestinationDao(database: AppDatabase): DestinationDao {
        return database.destinationDao()
    }
    
    @Provides
    @Singleton
    fun provideDestinationRepository(destinationDao: DestinationDao): DestinationRepository {
        return DestinationRepository(destinationDao)
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
