package com.example.elderease.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R

class CustomizationActivity : AppCompatActivity() {

    private lateinit var switchNotificationDots: Switch
    private lateinit var switchShowAllApps: Switch
    private lateinit var switchDirectCall: Switch
    private lateinit var switchCaregiver: Switch
    private lateinit var switchVibrationOnTap: Switch
    private lateinit var switchVoiceFeedback: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        val prefs = getSharedPreferences("elder_settings", Context.MODE_PRIVATE)

        // Find switches
        switchNotificationDots = findViewById(R.id.switchNotificationDots)
        switchShowAllApps = findViewById(R.id.switchShowAllApps)
        switchDirectCall = findViewById(R.id.switchDirectCall)
        switchCaregiver = findViewById(R.id.switchCaregiver)
        switchVibrationOnTap = findViewById(R.id.switchVibrationOnTap)
        switchVoiceFeedback = findViewById(R.id.switchVoiceFeedback)

        // Load saved values
        switchNotificationDots.isChecked = prefs.getBoolean("notification_dots", true)
        switchShowAllApps.isChecked = prefs.getBoolean("show_all_apps", true)
        switchDirectCall.isChecked = prefs.getBoolean("direct_call", false)
        switchCaregiver.isChecked = prefs.getBoolean("caregiver_enabled", true)
        switchVibrationOnTap.isChecked = prefs.getBoolean("vibration_on_tap", false)
        switchVoiceFeedback.isChecked = prefs.getBoolean("voice_feedback", false)

        // Save settings when toggled
        switchNotificationDots.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notification_dots", isChecked).apply()
        }

        switchShowAllApps.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_all_apps", isChecked).apply()
        }

        switchDirectCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("direct_call", isChecked).apply()
        }

        switchCaregiver.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("caregiver_enabled", isChecked).apply()
        }

        switchVibrationOnTap.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_on_tap", isChecked).apply()
        }

        switchVoiceFeedback.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_feedback", isChecked).apply()
        }
    }
}