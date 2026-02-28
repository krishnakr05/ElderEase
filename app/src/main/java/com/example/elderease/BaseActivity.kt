package com.example.elderease

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.data.storage.AccessibilityPrefs
import java.util.Locale

open class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var accessibilityPrefs: AccessibilityPrefs
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accessibilityPrefs = AccessibilityPrefs(this)
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            tts.setSpeechRate(0.9f)
        }
    }

    protected fun speak(message: String) {
        if (!accessibilityPrefs.isVoiceEnabled()) return

        if (::tts.isInitialized) {
            tts.stop() // prevent overlap
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    protected fun vibrate() {
        if (!accessibilityPrefs.isVibrationEnabled()) return

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(80)
        }
    }

    protected fun speakAndRun(message: String, action: () -> Unit) {
        vibrate()
        speak(message)
        handler.postDelayed({
            action()
        }, 400)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}