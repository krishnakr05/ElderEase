package com.example.elderease.data.storage

import android.content.Context

class CaregiverPrefs(context: Context) {

    private val prefs =
        context.getSharedPreferences("caregiver_prefs", Context.MODE_PRIVATE)

    // Save caregiver PIN
    fun savePin(pin: String) {
        prefs.edit().putString("pin", pin).apply()
    }

    // Get caregiver PIN
    fun getPin(): String {
        return prefs.getString("pin", "1234") ?: "1234"
    }

    // Enable / Disable emergency feature
    fun setEmergencyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("emergency", enabled).apply()
    }

    // Check if emergency is enabled
    fun isEmergencyEnabled(): Boolean {
        return prefs.getBoolean("emergency", true)
    }
}
