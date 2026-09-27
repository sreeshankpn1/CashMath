package com.cashmath.app.platform

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale

class PlatformAudio(private val activity: Activity) {
    fun startVoiceInput(onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            onResult("")
        }
    }
}
