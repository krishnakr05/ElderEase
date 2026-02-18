package com.example.elderease.ui.emergency

import android.content.Context

class EmergencyPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("elder_prefs", Context.MODE_PRIVATE)

    fun saveEmergencyNumbers(num1: String, num2: String, num3: String) {
        prefs.edit()
            .putString("emergency1", num1)
            .putString("emergency2", num2)
            .putString("emergency3", num3)
            .apply()
    }

    fun getEmergencyNumbers(): List<String> {
        val num1 = prefs.getString("emergency1", "9400692991") ?: ""
        val num2 = prefs.getString("emergency2", "9656098358") ?: ""
        val num3 = prefs.getString("emergency3", "8714199288") ?: ""

        return listOf(num1, num2, num3)
    }
}
