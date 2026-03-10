package com.example.elderease.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R

class CustomizationActivity : AppCompatActivity() {

    private lateinit var switchShowAllApps: Switch
    private lateinit var switchNotificationDots: Switch
    private lateinit var switchDirectCall: Switch
    private lateinit var switchCaregiver: Switch
    private lateinit var switchVibration: Switch
    private lateinit var switchVoiceFeedback: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        val prefs = getSharedPreferences("elder_settings", Context.MODE_PRIVATE)

        switchShowAllApps = findViewById(R.id.switchShowAllApps)
        switchNotificationDots = findViewById(R.id.switchNotificationDots)
        switchDirectCall = findViewById(R.id.switchDirectCall)
        switchCaregiver = findViewById(R.id.switchCaregiver)
        switchVibration = findViewById(R.id.switchVibrationOnTap)
        switchVoiceFeedback = findViewById(R.id.switchVoiceFeedback)

        // Default states
        switchShowAllApps.isChecked = prefs.getBoolean("show_all_apps", true)
        switchNotificationDots.isChecked = true
        switchDirectCall.isChecked = prefs.getBoolean("direct_call", false)
        switchCaregiver.isChecked = prefs.getBoolean("caregiver_enabled", false)
        switchVibration.isChecked = false
        switchVoiceFeedback.isChecked = false

        // Toggle View All Apps
        switchShowAllApps.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_all_apps", isChecked).apply()
            setResult(RESULT_OK)
        }

        // Notification dots always ON for now
        switchNotificationDots.isChecked = true
        switchNotificationDots.isEnabled = false

        switchDirectCall.setOnCheckedChangeListener { _, isChecked ->

            prefs.edit()
                .putBoolean("direct_call", isChecked)
                .apply()
        }

        // Caregiver toggle
        switchCaregiver.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("caregiver_enabled", isChecked).apply()
        }

        // Vibration (logic later)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_on_tap", isChecked).apply()
        }

        // Voice feedback (logic later)
        switchVoiceFeedback.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_feedback", isChecked).apply()
        }
    }
}