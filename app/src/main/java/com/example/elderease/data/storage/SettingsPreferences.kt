package com.example.elderease.data.storage

import android.content.Context

class SettingsPreferences(context: Context) {

    private val prefs =
        context.getSharedPreferences("elder_settings", Context.MODE_PRIVATE)

    var showNotificationDots: Boolean
        get() = prefs.getBoolean("show_notification_dots", true)
        set(value) = prefs.edit().putBoolean("show_notification_dots", value).apply()

    var showAllApps: Boolean
        get() = prefs.getBoolean("show_all_apps", true)
        set(value) = prefs.edit().putBoolean("show_all_apps", value).apply()

    var directCallEnabled: Boolean
        get() = prefs.getBoolean("direct_call", false)
        set(value) = prefs.edit().putBoolean("direct_call", value).apply()
}