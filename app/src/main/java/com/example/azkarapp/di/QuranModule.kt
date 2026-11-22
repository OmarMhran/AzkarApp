package com.example.azkarapp.di

import android.content.Context
import androidx.room.Room
import com.example.azkarapp.data.local.quran.HafzDao
import com.example.azkarapp.data.local.quran.QuranDao
import com.example.azkarapp.data.local.quran.QuranDatabase
import com.example.azkarapp.data.repo.QuranRepositoryImpl
import com.example.azkarapp.domain.repository.QuranRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuranBindingsModule {
    @Binds
    @Singleton
    abstract fun bindQuranRepository(impl: QuranRepositoryImpl): QuranRepository
}

@Module
@InstallIn(SingletonComponent::class)
object QuranDatabaseModule {

    @Provides
    @Singleton
    fun provideQuranDatabase(@ApplicationContext context: Context): QuranDatabase =
        Room.databaseBuilder(context, QuranDatabase::class.java, "quran_database.db")
            .addMigrations(QuranDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigrationFrom(1, 2) // نسخ قديمة، والبيانات بتتعاد من الـ assets
            .build()

    @Provides
    fun provideQuranDao(database: QuranDatabase): QuranDao = database.quranDao()

    @Provides
    fun provideHafzDao(database: QuranDatabase): HafzDao = database.hafzDao()
}
