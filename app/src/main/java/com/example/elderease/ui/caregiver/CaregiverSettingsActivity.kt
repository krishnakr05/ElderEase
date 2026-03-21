package com.example.elderease.ui.caregiver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.BaseActivity
class CaregiverSettingsActivity : BaseActivity() {

    private lateinit var prefs: CaregiverPrefs

    private lateinit var etNewPin: EditText
    private lateinit var etConfirmPin: EditText

    private lateinit var btnSavePin: Button
    private lateinit var btnSaveSettings: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_caregiver_settings)

        // Initialize preferences
        prefs = CaregiverPrefs(this)

        // 🔐 Block unauthorized access (Task 4)
        if (!intent.getBooleanExtra("AUTH_OK", false)) {

            val loginIntent = Intent(this, CaregiverLoginActivity::class.java)

            startActivity(loginIntent)

            finish()

            return
        }

        // Bind UI
        etNewPin = findViewById(R.id.etNewPin)
        etConfirmPin = findViewById(R.id.etConfirmPin)

        btnSavePin = findViewById(R.id.btnSavePin)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)


        // Change PIN button
        btnSavePin.setOnClickListener {

            changePin()
        }


        // Save other settings (future use)
        btnSaveSettings.setOnClickListener {

            showToast("Settings saved successfully")
        }
    }


    // Handle PIN change logic
    private fun changePin() {

        val newPin = etNewPin.text.toString().trim()
        val confirmPin = etConfirmPin.text.toString().trim()

        when {

            newPin.isEmpty() || confirmPin.isEmpty() -> {
                showToast("Please enter PIN in both fields")
            }

            newPin.length < 4 -> {
                showToast("PIN must be at least 4 digits")
            }

            newPin != confirmPin -> {
                showToast("PINs do not match")
            }

            isWeakPin(newPin) -> {
                showToast("Choose a stronger PIN")
            }

            else -> {

                prefs.savePin(newPin)

                showToast("PIN changed successfully")

                clearFields()
            }
        }
    }


    // Check weak PINs
    private fun isWeakPin(pin: String): Boolean {

        val weakPins = listOf(
            "1234",
            "0000",
            "1111",
            "2222",
            "1212"
        )

        return weakPins.contains(pin)
    }


    // Clear input fields
    private fun clearFields() {

        etNewPin.text.clear()
        etConfirmPin.text.clear()
    }


    // Toast helper
    private fun showToast(msg: String) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
