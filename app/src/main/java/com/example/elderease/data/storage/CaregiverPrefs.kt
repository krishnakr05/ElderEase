package com.example.elderease.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CaregiverPrefs(context: Context) {

    private lateinit var securePrefs: SharedPreferences

    init {

        try {

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                context,
                "secure_caregiver_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        } catch (e: Exception) {

            // Delete corrupted encrypted file
            context.deleteSharedPreferences("secure_caregiver_prefs")

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                context,
                "secure_caregiver_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    // ---------------- PIN STORAGE ----------------

    fun savePin(pin: String) {
        securePrefs.edit()
            .putString("caregiver_pin", pin)
            .apply()
    }

    fun getPin(): String? {
        return securePrefs.getString("caregiver_pin", null)
    }

    fun isPinSet(): Boolean {
        return getPin() != null
    }

    fun clearPin() {
        securePrefs.edit()
            .remove("caregiver_pin")
            .apply()
    }

    // ---------------- LOCK SYSTEM ----------------

    fun saveFailedAttempts(count: Int) {
        securePrefs.edit()
            .putInt("failed_attempts", count)
            .apply()
    }

    fun getFailedAttempts(): Int {
        return securePrefs.getInt("failed_attempts", 0)
    }

    fun saveLockTime(time: Long) {
        securePrefs.edit()
            .putLong("lock_time", time)
            .apply()
    }

    fun getLockTime(): Long {
        return securePrefs.getLong("lock_time", 0L)
    }

    fun resetLock() {
        securePrefs.edit()
            .remove("failed_attempts")
            .remove("lock_time")
            .apply()
    }
}