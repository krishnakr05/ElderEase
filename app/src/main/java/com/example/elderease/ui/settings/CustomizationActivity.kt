package com.example.elderease.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
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

        findViewById<LinearLayout>(R.id.rowHomeCustomization).setOnClickListener {
            startActivity(Intent(this, HomeCustomizationActivity::class.java))
        }

        switchShowAllApps = findViewById(R.id.switchShowAllApps)
        switchNotificationDots = findViewById(R.id.switchNotificationDots)
        switchDirectCall = findViewById(R.id.switchDirectCall)
        switchCaregiver = findViewById(R.id.switchCaregiver)
        switchVibration = findViewById(R.id.switchVibrationOnTap)
        switchVoiceFeedback = findViewById(R.id.switchVoiceFeedback)

        switchShowAllApps.isChecked = prefs.getBoolean("show_all_apps", true)
        switchNotificationDots.isChecked = true
        switchDirectCall.isChecked = prefs.getBoolean("direct_call", false)
        switchCaregiver.isChecked = prefs.getBoolean("caregiver_enabled", false)
        switchVibration.isChecked = prefs.getBoolean("vibration_enabled", false)
        switchVoiceFeedback.isChecked = prefs.getBoolean("voice_enabled", false)

        switchShowAllApps.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_all_apps", isChecked).apply()
            setResult(RESULT_OK)
        }

        switchNotificationDots.isChecked = true
        switchNotificationDots.isEnabled = false

        switchDirectCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("direct_call", isChecked)
                .apply()
        }

        switchCaregiver.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("caregiver_enabled", isChecked).apply()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        switchVoiceFeedback.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_enabled", isChecked).apply()
        }
    }
}