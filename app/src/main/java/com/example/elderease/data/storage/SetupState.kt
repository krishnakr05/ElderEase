package com.example.elderease.data.storage

import android.content.Context

class SetupState(context: Context) {

    private val prefs =
        context.getSharedPreferences("setup_state", Context.MODE_PRIVATE)

    fun markAppsDone() {
        prefs.edit().putBoolean("apps_done", true).apply()
    }

    fun isAppsDone(): Boolean =
        prefs.getBoolean("apps_done", false)

    fun markFavouriteContactsDone() {
        prefs.edit().putBoolean("fav_contacts_done", true).apply()
    }

    fun isFavouriteContactsDone(): Boolean =
        prefs.getBoolean("fav_contacts_done", false)

    fun markContactsDone() {
        prefs.edit().putBoolean("contacts_done", true).apply()
    }

    fun isContactsDone(): Boolean =
        prefs.getBoolean("contacts_done", false)

    fun markPinDone() {
        prefs.edit().putBoolean("pin_done", true).apply()
    }

    fun isPinDone(): Boolean =
        prefs.getBoolean("pin_done", false)
}