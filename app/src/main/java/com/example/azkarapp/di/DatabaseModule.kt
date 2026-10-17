package com.example.azkarapp.di

import android.content.Context
import androidx.room.Room
import com.example.azkarapp.data.local.assets.JsonAssetReader
import com.example.azkarapp.data.local.database.AzkarDatabase
import com.example.azkarapp.data.local.database.dao.CategoryDao
import com.example.azkarapp.data.local.database.dao.GroupDao
import com.example.azkarapp.data.local.database.dao.MisbahaDao
import com.example.azkarapp.data.local.database.dao.NamesDao
import com.example.azkarapp.data.local.database.dao.SupplicationDao
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
    fun provideDatabase(@ApplicationContext context: Context): AzkarDatabase {
        return Room.databaseBuilder(
            context,
            AzkarDatabase::class.java,
            "azkar_database.db"
        )
            .fallbackToDestructiveMigrationFrom(1, 2)
            .build()
    }

    @Provides
    @Singleton
    fun provideJsonAssetReader(@ApplicationContext context: Context): JsonAssetReader {
        return JsonAssetReader(context)
    }

    @Provides
    fun provideGroupDao(database: AzkarDatabase): GroupDao = database.groupDao()

    @Provides
    fun provideCategoryDao(database: AzkarDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideSupplicationDao(database: AzkarDatabase): SupplicationDao =
        database.supplicationDao()

    @Provides
    fun provideMisbahaDao(database: AzkarDatabase): MisbahaDao = database.misbahaDao()

    @Provides
    fun provideNamesDao(database: AzkarDatabase): NamesDao = database.namesDao()

}