package com.example.elderease.ui.caregiver

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
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

    companion object {
        const val MODE_SET = "SET_PIN"
        const val MODE_VERIFY = "VERIFY_PIN"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var mode: String

    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnAction: Button

    private val MAX_ATTEMPTS = 3
    private var attemptsLeft = MAX_ATTEMPTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_login)

        // SharedPreferences
        prefs = getSharedPreferences("caregiver_prefs", MODE_PRIVATE)

        // Read mode ONCE
        mode = intent.getStringExtra("MODE") ?: MODE_VERIFY

        // Bind views
        tvTitle = findViewById(R.id.tvTitle)
        tvError = findViewById(R.id.tvError)
        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnAction = findViewById(R.id.btnSetPin)

        updateUIForMode()

        btnAction.setOnClickListener {
            handlePin()
        }
    }

    private fun updateUIForMode() {
        when (mode) {

            MODE_SET -> {
                tvTitle.text = "Set Caregiver PIN"
                etPin1.hint = "Enter new PIN"
                etPin2.hint = "Confirm new PIN"
                etPin2.visibility = View.VISIBLE
                btnAction.text = "Set PIN"
            }

            MODE_VERIFY -> {
                tvTitle.text = "Verify Caregiver PIN"
                etPin1.hint = "Enter PIN"
                etPin2.visibility = View.GONE
                btnAction.text = "Verify"
            }
        }
    }

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = if (mode == MODE_SET) etPin2.text.toString().trim() else pin1

        if (pin1.isEmpty() || pin2.isEmpty()) {
            tvError.text = "Please enter PIN"
            return
        }

        if (mode == MODE_SET && pin1 != pin2) {
            tvError.text = "PINs do not match"
            return
        }

        val savedPin = prefs.getString("pin", null)

        when (mode) {

            MODE_SET -> {
                if (savedPin != null) {
                    tvError.text = "PIN already set"
                    return
                }

                prefs.edit()
                    .putString("pin", pin1)
                    .apply()

                // Mark setup complete
                val setupPrefs = getSharedPreferences(
                    SetupAppsActivity.PREFS_NAME,
                    MODE_PRIVATE
                )
                setupPrefs.edit()
                    .putBoolean(SetupAppsActivity.KEY_SETUP_COMPLETE, true)
                    .apply()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

            MODE_VERIFY -> {
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
                    if (attemptsLeft <= 0) {
                        btnAction.isEnabled = false
                    }
                }
            }
        }
    }
}