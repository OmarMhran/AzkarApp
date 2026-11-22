package com.example.azkarapp.di

import com.example.azkarapp.data.repo.HafzRepositoryImpl
import com.example.azkarapp.data.speech.AndroidSpeechSource
import com.example.azkarapp.domain.recitation.SpeechSource
import com.example.azkarapp.domain.repository.HafzRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HafzBindingsModule {
    @Binds
    @Singleton
    abstract fun bindHafzRepository(impl: HafzRepositoryImpl): HafzRepository

    @Binds
    @Singleton
    abstract fun bindSpeechSource(impl: AndroidSpeechSource): SpeechSource
}
