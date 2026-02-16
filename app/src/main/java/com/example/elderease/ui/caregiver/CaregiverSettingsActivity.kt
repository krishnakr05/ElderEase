package com.example.elderease.ui.caregiver

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.data.storage.CaregiverPrefs

class CaregiverSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: CaregiverPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_caregiver_settings)

        // Initialize SharedPreferences helper
        prefs = CaregiverPrefs(this)

        // Get UI elements
        val etNewPin = findViewById<EditText>(R.id.etNewPin)
        val btnSavePin = findViewById<Button>(R.id.btnSavePin)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)

        // Save PIN button
        btnSavePin.setOnClickListener {

            val newPin = etNewPin.text.toString().trim()

            if (newPin.length >= 4) {

                prefs.savePin(newPin)

                Toast.makeText(
                    this,
                    "PIN saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                etNewPin.text.clear()

            } else {

                Toast.makeText(
                    this,
                    "PIN must be at least 4 digits",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Save other settings (for future use)
        btnSaveSettings.setOnClickListener {

            Toast.makeText(
                this,
                "Settings saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
