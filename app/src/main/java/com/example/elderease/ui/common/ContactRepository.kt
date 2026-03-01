package com.example.elderease.ui.common

import android.content.Context

object ContactRepository {

    private const val PREF_NAME = "elder_ease_prefs"
    private const val KEY_SELECTED_PHONES = "selected_contact_phones"

    fun saveSelectedPhones(context: Context, phones: List<String>) {
        val data = phones.joinToString(",")
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_PHONES, data)
            .apply()
    }

    fun loadSelectedPhones(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_PHONES, "") ?: ""

        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}