package com.example.azkarapp.domain.usecase.hafz

import com.example.azkarapp.domain.model.HafzProgress
import com.example.azkarapp.domain.repository.HafzRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHafzProgressUseCase @Inject constructor(private val repository: HafzRepository) {
    operator fun invoke(): Flow<HafzProgress> = repository.observeProgress()
}

class SetPageMemorizedUseCase @Inject constructor(private val repository: HafzRepository) {
    suspend operator fun invoke(page: Int, memorized: Boolean) =
        repository.setPageMemorized(page, memorized)
}

class GetLastHafzPageUseCase @Inject constructor(private val repository: HafzRepository) {
    suspend operator fun invoke(): Int? = repository.getLastPage()
}

class SaveLastHafzPageUseCase @Inject constructor(private val repository: HafzRepository) {
    suspend operator fun invoke(page: Int) = repository.saveLastPage(page)
}