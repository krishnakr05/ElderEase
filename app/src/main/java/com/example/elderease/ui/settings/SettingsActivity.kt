package com.example.elderease.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import com.example.elderease.BaseActivity
import com.example.elderease.R
import com.example.elderease.data.storage.AccessibilityPrefs
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class SettingsActivity : BaseActivity() {

    private lateinit var accessibilityPrefs: AccessibilityPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        accessibilityPrefs = AccessibilityPrefs(this)

        val switchVoice = findViewById<Switch>(R.id.switchVoice)
        val switchVibration = findViewById<Switch>(R.id.switchVibration)

        // Load saved states
        switchVoice.isChecked = accessibilityPrefs.isVoiceEnabled()
        switchVibration.isChecked = accessibilityPrefs.isVibrationEnabled()

        // Voice toggle
        switchVoice.setOnCheckedChangeListener { _, isChecked ->
            accessibilityPrefs.setVoiceEnabled(isChecked)

            if (isChecked) {
                speak("Voice feedback enabled")
            } else {
                vibrate()
            }
        }

        // Vibration toggle
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            accessibilityPrefs.setVibrationEnabled(isChecked)

            if (isChecked) {
                speak("Vibration enabled")
            } else {
                speak("Vibration disabled")
            }
        }

        findViewById<Button>(R.id.btnEditApps).setOnClickListener {
            speakAndRun("Opening app setup") {
                startActivity(
                    Intent(this, SetupAppsActivity::class.java).apply {
                        putExtra("MODE", "EDIT")
                    }
                )
            }
        }

        findViewById<Button>(R.id.btnEditContacts).setOnClickListener {
            speakAndRun("Opening contact setup") {
                startActivity(
                    Intent(this, ContactSetupActivity::class.java).apply {
                        putExtra("MODE", "EDIT")
                    }
                )
            }
        }

        findViewById<Button>(R.id.btnCustomization).setOnClickListener {
            vibrate()
            speak("Customization coming soon")
            Toast.makeText(
                this,
                "Customization coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<Button>(R.id.btnDefaultLauncher).setOnClickListener {
            vibrate()
            speak("Default launcher setup coming soon")
            Toast.makeText(
                this,
                "Default launcher setup coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        speak("Settings screen")
    }
}