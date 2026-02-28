package com.example.elderease.data.storage

import android.content.Context

class AccessibilityPrefs(context: Context) {

    private val prefs =
        context.getSharedPreferences("accessibility_prefs", Context.MODE_PRIVATE)

    fun isVoiceEnabled(): Boolean {
        return prefs.getBoolean("voice_enabled", true)
    }

    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean("vibration_enabled", true)
    }

    fun setVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voice_enabled", enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }
}