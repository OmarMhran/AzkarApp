package com.example.azkar.di

import com.example.azkar.ui.home.calender.data.CalenderDataClass
import com.example.azkar.ui.home.calender.repo.CalenderRepository
import com.example.azkar.ui.home.calender.repo.CalenderRepositoryImpl
import com.example.azkar.ui.home.settings.data.SettingsData
import com.example.azkar.ui.home.settings.repo.SettingsRepository
import com.example.azkar.ui.home.settings.repo.SettingsRepositoryImpl
import com.example.azkar.ui.home.tasbih.repo.TasbihRepository
import com.example.azkar.ui.home.tasbih.repo.TasbihRepositoryImpl
import com.example.azkar.ui.location.repo.LocationRepository
import com.example.azkar.ui.location.repo.LocationRepositoryImpl
import com.example.azkar.util.shared.SharedPreferenceDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent


@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {

    @Provides
    fun provideLocationRepoImpl(sharedPreferenceDataSource: SharedPreferenceDataSource) : LocationRepository{
        return LocationRepositoryImpl(sharedPreferenceDataSource)
    }


    @Provides
    fun provideTasbihRepoImpl(sharedPreferenceDataSource: SharedPreferenceDataSource):TasbihRepository{
        return TasbihRepositoryImpl(sharedPreferenceDataSource)
    }



    @Provides
    fun provideCalenderRepoImpl(data: CalenderDataClass): CalenderRepository{
        return CalenderRepositoryImpl(data)
    }

    @Provides
    fun provideSettingsRepoImpl(data: SettingsData,shared: SharedPreferenceDataSource):SettingsRepository{
        return SettingsRepositoryImpl(data,shared)
    }

    @Provides
    fun provideSettingsData():SettingsData{
        return SettingsData()
    }

    @Provides
    fun provideCalenderData(sharedPreferenceDataSource: SharedPreferenceDataSource): CalenderDataClass{
        return CalenderDataClass(sharedPreferenceDataSource)
    }
}