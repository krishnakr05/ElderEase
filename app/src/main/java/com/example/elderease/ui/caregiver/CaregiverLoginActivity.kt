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

class CaregiverLoginActivity : AppCompatActivity() {

    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnSetPin: Button
    private lateinit var tvError: TextView
    private lateinit var tvTitle: TextView

    private lateinit var prefs: SharedPreferences

    private val MAX_ATTEMPTS = 3
    private var attemptsLeft = MAX_ATTEMPTS

    private var mode: String = "NORMAL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("caregiver_prefs", MODE_PRIVATE)
        mode = intent.getStringExtra("MODE") ?: "NORMAL"

        val isVerified = prefs.getBoolean("verified", false)

        if (isVerified && mode == "NORMAL") {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_caregiver_login)

        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnSetPin = findViewById(R.id.btnSetPin)
        tvError = findViewById(R.id.tvError)
        tvTitle = findViewById(R.id.tvTitle)

        // Settings verification mode
        if (mode == "VERIFY_SETTINGS") {
            tvTitle.text = "Enter Caregiver PIN"
            etPin2.visibility = View.GONE
            btnSetPin.text = "VERIFY PIN"
        }

        btnSetPin.setOnClickListener {
            handlePin()
        }
    }

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = etPin2.text.toString().trim()

        val savedPin = prefs.getString("pin", null)

        // First time setup
        if (savedPin == null) {

            if (pin1.isEmpty() || pin2.isEmpty()) {
                tvError.text = "Please enter PIN in both fields"
                return
            }

            if (pin1 != pin2) {
                tvError.text = "PINs do not match"
                clearFields()
                return
            }

            prefs.edit()
                .putString("pin", pin1)
                .putBoolean("verified", true)
                .apply()

            Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show()

            goNext()

        } else {

            // Verification
            if (pin1.isEmpty()) {
                tvError.text = "Please enter PIN"
                return
            }

            if (pin1 == savedPin) {

                prefs.edit()
                    .putBoolean("verified", true)
                    .apply()

                goNext()

            } else {

                attemptsLeft--

                if (attemptsLeft > 0) {
                    tvError.text = "Wrong PIN. Attempts left: $attemptsLeft"
                    clearFields()
                } else {
                    tvError.text = "Too many attempts. Access locked."
                    btnSetPin.isEnabled = false
                }
            }
        }
    }

    private fun clearFields() {
        etPin1.text.clear()
        etPin2.text.clear()
    }

    private fun goNext() {

        if (mode == "VERIFY_SETTINGS") {

            val intent = Intent(this, CaregiverSettingsActivity::class.java)
            intent.putExtra("AUTH_OK", true)

            startActivity(intent)

        } else {

            startActivity(Intent(this, HomeActivity::class.java))
        }

        finish()
    }
}
