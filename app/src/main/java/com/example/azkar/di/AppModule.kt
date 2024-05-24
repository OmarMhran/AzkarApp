package com.example.azkar.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azkar.R
import com.example.azkar.databinding.ActivityHomeBinding
import com.example.azkar.ui.supplications.AzkarDetailsAdapter
import com.example.azkar.ui.home.settings.data.SettingColorsAdapter
import com.example.azkar.ui.home.settings.data.SettingThemeAdapter
import com.example.azkar.ui.home.azkar.data.AzkarData
import com.example.azkar.ui.home.azkar.data.AzkarAdapter
import com.example.azkar.ui.category.CategoryAdapter
import com.example.azkar.ui.home.calender.data.PrayerAdapter
import com.example.azkar.util.AzkarUtility
import com.example.azkar.util.PrayerTimes
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun providesPrayerTimes(): PrayerTimes {
        return PrayerTimes()
    }


    @Singleton
    @Provides
    fun providesIslamicCalender(): UmmalquraCalendar {
        return UmmalquraCalendar()
    }


    @Singleton
    @Provides
    fun providesAzkarUtility(@ApplicationContext context: Context ,filename: String ):String?{
        return AzkarUtility(context).readJSONFromSection(filename)
    }

    @Singleton
    @Provides
    fun providesCategoryAdapter(@ApplicationContext context: Context): CategoryAdapter {
        return CategoryAdapter(context)
    }

    @Singleton
    @Provides
    fun providesSettingColorsAdapter(): SettingColorsAdapter {
        return SettingColorsAdapter()
    }


    @Singleton
    @Provides
    fun providesSettingThemeAdapter(): SettingThemeAdapter {
        return SettingThemeAdapter()
    }


    @Singleton
    @Provides
    fun providesSectionsAdapter(@ApplicationContext context: Context): AzkarAdapter {
        return AzkarAdapter(context)
    }

    @Singleton
    @Provides
    fun providesPrayerAdapter(@ApplicationContext context: Context): PrayerAdapter {
        return PrayerAdapter(context)
    }

    @Singleton
    @Provides
    fun providesAzkarDetailsAdapter(): AzkarDetailsAdapter {
        return AzkarDetailsAdapter()
    }




    @Singleton
    @Provides
    fun providesLinearLayoutManager(@ApplicationContext context: Context):LinearLayoutManager{
        return LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL,false)
    }

    @Singleton
    @Provides
    fun providesGridLayoutManager(@ApplicationContext context: Context): GridLayoutManager {
        return GridLayoutManager(context , 3, GridLayoutManager.VERTICAL, false)
    }


    @Singleton
    @Provides
    fun providesDataClass(@ApplicationContext context: Context): AzkarData {
        return AzkarData(context)
    }







}
