package com.example.azkarapp.domain.recitation

import kotlinx.coroutines.flow.Flow

enum class SpeechFailure {
    NO_SPEECH,
    RETRYABLE,
    NETWORK,
    NO_PERMISSION,
    UNAVAILABLE
}

sealed interface SpeechEvent {
    data object Ready : SpeechEvent
    data class Partial(val text: String) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    data class Failure(val reason: SpeechFailure) : SpeechEvent
}


interface SpeechSource {
    fun isAvailable(): Boolean
    fun listen(): Flow<SpeechEvent>
}
