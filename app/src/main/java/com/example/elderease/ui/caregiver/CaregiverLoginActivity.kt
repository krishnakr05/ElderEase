package com.example.elderease.ui.caregiver

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.ui.settings.SettingsActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class CaregiverLoginActivity : AppCompatActivity() {

    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnSetPin: Button
    private lateinit var tvError: TextView

    private lateinit var prefs: SharedPreferences

    private lateinit var mode: String

    private val MAX_ATTEMPTS = 3
    private var attemptsLeft = MAX_ATTEMPTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences
        prefs = getSharedPreferences("caregiver_prefs", MODE_PRIVATE)

        // If already verified, skip login
        mode = intent.getStringExtra("MODE") ?: "VERIFY_PIN"

        setContentView(R.layout.activity_caregiver_login)

        // Initialize views
        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnSetPin = findViewById(R.id.btnSetPin)
        tvError = findViewById(R.id.tvError)

        btnSetPin.setOnClickListener {
            handlePin()
        }
    }

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = etPin2.text.toString().trim()

        if (pin1.isEmpty() || pin2.isEmpty()) {
            tvError.text = "Please enter PIN in both fields"
            return
        }

        if (pin1 != pin2) {
            tvError.text = "PINs do not match"
            return
        }

        val savedPin = prefs.getString("pin", null)

        // 🔹 SET PIN (first-time setup)
        if (mode == "SET_PIN") {

            if (savedPin != null) {
                tvError.text = "PIN already set"
                return
            }

            prefs.edit()
                .putString("pin", pin1)
                .apply()

            // ✅ mark setup complete HERE
            val setupPrefs = getSharedPreferences(SetupAppsActivity.PREFS_NAME, MODE_PRIVATE)
            setupPrefs.edit()
                .putBoolean(SetupAppsActivity.KEY_SETUP_COMPLETE, true)
                .apply()

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // 🔹 VERIFY PIN (Settings access)
        if (mode == "VERIFY_PIN") {

            if (savedPin == null) {
                tvError.text = "No caregiver PIN set"
                return
            }

            if (pin1 == savedPin) {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            } else {
                attemptsLeft--
                tvError.text = "Wrong PIN. Attempts left: $attemptsLeft"
                if (attemptsLeft <= 0) btnSetPin.isEnabled = false
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
