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

class CaregiverLoginActivity : AppCompatActivity() {

    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnSetPin: Button
    private lateinit var tvError: TextView

    private lateinit var prefs: SharedPreferences

    private val MAX_ATTEMPTS = 3
    private var attemptsLeft = MAX_ATTEMPTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences
        prefs = getSharedPreferences("caregiver_prefs", MODE_PRIVATE)

        // If already verified, skip login
        val isVerified = prefs.getBoolean("verified", false)

        if (isVerified) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

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

        // Check empty
        if (pin1.isEmpty() || pin2.isEmpty()) {
            tvError.text = "Please enter PIN in both fields"
            return
        }

        // Check match
        if (pin1 != pin2) {
            tvError.text = "PINs do not match"
            return
        }

        val savedPin = prefs.getString("pin", null)

        // First time: set PIN
        if (savedPin == null) {

            prefs.edit()
                .putString("pin", pin1)
                .putBoolean("verified", true)
                .apply()

            Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show()

            goToHome()

        } else {

            // Verify existing PIN
            if (pin1 == savedPin) {

                prefs.edit()
                    .putBoolean("verified", true)
                    .apply()

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                goToHome()

            } else {

                attemptsLeft--

                if (attemptsLeft > 0) {
                    tvError.text = "Wrong PIN. Attempts left: $attemptsLeft"
                } else {
                    tvError.text = "Too many attempts. Try again later."
                    btnSetPin.isEnabled = false
                }
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
