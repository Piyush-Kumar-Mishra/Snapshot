package com.example.snapshot.di

import android.content.Context
import androidx.room.Room
import com.example.snapshot.data.local.AppDatabase
import com.example.snapshot.data.local.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "snapshot_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }
}
