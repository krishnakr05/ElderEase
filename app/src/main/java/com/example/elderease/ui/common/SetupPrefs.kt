package com.example.elderease.ui.common

import android.content.Context

object SetupPrefs {

    private const val PREF_NAME = "elder_setup_prefs"
    private const val KEY_SETUP_DONE = "setup_completed"

    fun setSetupCompleted(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SETUP_DONE, true)
            .apply()
    }

    fun isSetupCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SETUP_DONE, false)
    }
}