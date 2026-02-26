package com.example.elderease.data.storage

import android.content.Context

class SetupState(context: Context) {

    private val prefs =
        context.getSharedPreferences("setup_state", Context.MODE_PRIVATE)

    fun isAppsDone(): Boolean =
        prefs.getBoolean("apps_done", false)

    fun isContactsDone(): Boolean =
        prefs.getBoolean("contacts_done", false)

    fun isPinDone(): Boolean =
        prefs.getBoolean("pin_done", false)

    fun markAppsDone() {
        prefs.edit().putBoolean("apps_done", true).apply()
    }

    fun markContactsDone() {
        prefs.edit().putBoolean("contacts_done", true).apply()
    }

    fun markPinDone() {
        prefs.edit().putBoolean("pin_done", true).apply()
    }
}