package com.example.azkarapp.data.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.azkarapp.domain.recitation.SpeechEvent
import com.example.azkarapp.domain.recitation.SpeechFailure
import com.example.azkarapp.domain.recitation.SpeechSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تعرّف صوتي بتاع أندرويد. بيفضّل المحرك اللي على الجهاز (أوفلاين، من غير ما التطبيق يكبر)
 * لو متاح، ولو حزمة العربي مش موجودة عليه بيرجع للمحرك العادي اللي بيستخدم النت.
 */
@Singleton
class AndroidSpeechSource @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechSource {

    /** بتتقفل أول ما المحرك المحلي يقول إن العربي مش متاحة عنده. */
    @Volatile
    private var onDeviceUsable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** لو المحرك رفض لهجة، بنجرّب اللي بعدها قبل ما نقول إن العربي مش متاحة. */
    @Volatile
    private var languageIndex = 0

    override fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override fun listen(): Flow<SpeechEvent> = callbackFlow {
        val useOnDevice = onDeviceUsable &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

        val language = LANGUAGES[languageIndex.coerceIn(0, LANGUAGES.lastIndex)]
        val recognizer = if (useOnDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechEvent.Ready)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.firstText()?.let { trySend(SpeechEvent.Partial(it)) }
            }

            override fun onResults(results: Bundle?) {
                trySend(SpeechEvent.Final(results.firstText().orEmpty()))
                close()
            }

            override fun onError(error: Int) {
                if (useOnDevice &&
                    (error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                            error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
                ) {
                    onDeviceUsable = false // المرة الجاية على المحرك العادي
                    trySend(SpeechEvent.Failure(SpeechFailure.RETRYABLE))
                    close()
                    return
                }
                val languageError = error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                        error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                if (languageError && languageIndex < LANGUAGES.lastIndex) {
                    languageIndex++ // نجرّب لهجة عربية تانية
                    trySend(SpeechEvent.Failure(SpeechFailure.RETRYABLE))
                    close()
                    return
                }
                trySend(SpeechEvent.Failure(mapError(error)))
                close()
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        recognizer.startListening(buildIntent(language, preferOffline = useOnDevice))

        awaitClose {
            recognizer.cancel()
            recognizer.destroy()
        }
    }.flowOn(Dispatchers.Main) // SpeechRecognizer لازم يتكلم من الـ main thread

    private fun buildIntent(language: String, preferOffline: Boolean) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        // المحرك العادي (Google) مع PREFER_OFFLINE ومن غير حزمة أوفلاين ممكن يرجّع
        // "اللغة غير متاحة" بدل ما يستخدم النت، فبنطلبها للمحرك المحلي بس.
        if (preferOffline) putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        // التلاوة فيها وقفات، فبنحاول نطوّل مهلة السكوت (بعض المحركات بتتجاهلها)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
    }

    private fun Bundle?.firstText(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun mapError(error: Int): SpeechFailure = when (error) {
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
        SpeechRecognizer.ERROR_NO_MATCH -> SpeechFailure.NO_SPEECH

        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechFailure.NO_PERMISSION

        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> SpeechFailure.UNAVAILABLE

        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        SpeechRecognizer.ERROR_SERVER -> SpeechFailure.NETWORK

        else -> SpeechFailure.RETRYABLE // audio / client / busy / too many requests / disconnected
    }

    private companion object {
        val LANGUAGES = listOf("ar-SA", "ar-EG", "ar")
    }
}